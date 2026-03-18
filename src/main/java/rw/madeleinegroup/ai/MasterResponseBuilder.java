package rw.madeleinegroup.ai;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MasterResponseBuilder {

    /**
     * Builds full response dynamically from live data. Proactive alerts as natural sentences when needed.
     * snapshotPrevious can be null; used for monthly comparison in PERFORMANCE.
     */
    public String build(String intent, String userMessage,
                        DeepReport report, LiveFinancialSnapshot snap,
                        boolean fr, List<Map<String, String>> messages,
                        LiveFinancialSnapshot snapshotPrevious) {
        String main = switch (intent) {
            case "PERFORMANCE" -> buildPerformance(report, snap, fr, snapshotPrevious);
            case "EXPENSES" -> buildExpenses(report, snap, fr);
            case "PROFIT" -> buildProfit(report, snap, fr);
            case "REVENUE" -> buildRevenue(report, snap, fr);
            case "RISKS" -> buildRisks(report, snap, fr);
            case "HEALTH" -> buildHealth(report, snap, fr, userMessage);
            case "PENDING" -> buildPending(report, snap, fr);
            case "BOOKINGS" -> buildBookings(report, snap, fr);
            case "CLIENTS" -> buildClients(report, snap, fr);
            case "BRANCHES" -> buildBranches(report, snap, fr);
            case "PROJECTION" -> buildProjection(report, snap, fr);
            case "QUICKWINS" -> buildQuickWins(report, snap, fr);
            case "WELCOME" -> isShortGreeting(userMessage)
                ? buildCasualGreeting(report, snap, fr)
                : buildWelcome(report, snap, fr);
            default -> buildFull(report, snap, fr, snapshotPrevious);
        };
        String alerts = proactiveAlertsBlock(report, snap, fr);
        return (alerts.isEmpty() ? "" : alerts + "\n\n") + main;
    }

    /** Grade from health score (0–100), not from profit margin. */
    private String healthScoreGrade(int score) {
        if (score >= 80) return "A+";
        if (score >= 60) return "B";
        if (score >= 40) return "C";
        if (score >= 20) return "D";
        return "F";
    }

    private String healthScoreGradeLabel(int score, boolean fr) {
        String g = healthScoreGrade(score);
        if (fr) {
            return switch (g) {
                case "A+" -> "excellent";
                case "B" -> "bon";
                case "C" -> "moyen";
                case "D" -> "à améliorer";
                default -> "critique";
            };
        }
        return switch (g) {
            case "A+" -> "excellent";
            case "B" -> "good";
            case "C" -> "fair";
            case "D" -> "needs improvement";
            default -> "critical";
        };
    }

    private boolean isShortGreeting(String msg) {
        if (msg == null) return true;
        String t = msg.trim();
        return t.length() <= 25 || t.split("\\s+").length <= 3;
    }

    /** Proactive alerts as one or two natural sentences using real data (Prompt 6). */
    private String proactiveAlertsBlock(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        boolean critical = "CRITICAL".equals(r.getRiskLevel()) || "HIGH".equals(r.getRiskLevel());
        boolean hasUrgent = r.getUrgentActions() != null && !r.getUrgentActions().isEmpty();
        boolean declining = "DECLINING".equals(r.getTrend()) || "STRONG_DECLINE".equals(r.getTrend());
        if (!critical && !hasUrgent && !declining && s.getOverdueBookings() == 0
            && !(s.getTotalIncome() > 0 && s.getPendingAmount() / s.getTotalIncome() > 0.25))
            return "";

        StringBuilder sb = new StringBuilder();
        if (s.getOverdueBookings() > 0) {
            double impact = r.getOverdueBookingsImpactRwf();
            if (fr)
                sb.append("Avant tout : vous avez ").append(s.getOverdueBookings()).append(" réservation(s) en retard (environ ").append(fmt(impact)).append(" RWF à risque). ");
            else
                sb.append("Quick heads-up: you have ").append(s.getOverdueBookings()).append(" overdue booking(s) (about ").append(fmt(impact)).append(" RWF at risk). ");
        }
        if (s.getTotalIncome() > 0 && s.getPendingAmount() / s.getTotalIncome() > 0.25) {
            if (fr)
                sb.append("Encaissements en attente : **").append(fmt(s.getPendingAmount())).append(" RWF** — envoyer des rappels aujourd’hui aiderait. ");
            else
                sb.append("Pending collections: **").append(fmt(s.getPendingAmount())).append(" RWF** — sending reminders today would help. ");
        }
        if (s.getProfitMargin() < 20) {
            if (fr)
                sb.append("La marge est basse (").append(f1(s.getProfitMargin())).append("%) ; pour viser 60%, il faudrait réduire les dépenses de **").append(fmt(r.getRwfToSaveForTargetMargin())).append(" RWF** ou augmenter le revenu de **").append(fmt(r.getRwfToEarnForTargetMargin())).append(" RWF**. ");
            else
                sb.append("Margin is low (").append(f1(s.getProfitMargin())).append("%); to reach 60% you’d need to cut expenses by **").append(fmt(r.getRwfToSaveForTargetMargin())).append(" RWF** or increase revenue by **").append(fmt(r.getRwfToEarnForTargetMargin())).append(" RWF**. ");
        }
        if (declining) {
            if (fr) sb.append("La tendance revenus est en baisse — prioriser les actions de croissance. ");
            else sb.append("Revenue trend is declining — prioritize growth actions. ");
        }
        return sb.toString().trim();
    }

    /** Warm, natural short reply for "hey", "salut", "how are you" — grade from health score only. */
    private String buildCasualGreeting(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        int score = r.getHealthScore();
        String gradeLabel = healthScoreGradeLabel(score, fr);
        if (fr) {
            return String.format("👋 Bonjour, ça va bien merci. %s Votre santé financière est à **%d/100** (%s). Je peux vous aider à analyser vos données en temps réel — demandez-moi par exemple une analyse du mois, les dépenses ou les réservations.", scoreEmoji(score), score, gradeLabel);
        } else {
            return String.format("👋 Hi there, doing well thanks. %s Your financial health is **%d/100** (%s). I’m here to help you analyze your real-time data — just ask for things like monthly analysis, expenses, or bookings.", scoreEmoji(score), score, gradeLabel);
        }
    }

    /** Dynamic monthly analysis: real numbers, comparison to last month, best branch, top expense (Prompt 2). */
    private String buildPerformance(DeepReport r, LiveFinancialSnapshot s, boolean fr, LiveFinancialSnapshot prev) {
        StringBuilder sb = new StringBuilder();
        if (fr) {
            sb.append("En regardant les chiffres de la période : ce mois vous avez gagné **").append(fmt(s.getTotalIncome())).append(" RWF** avec **").append(s.getTotalBookings()).append("** réservation(s) (dont **").append(s.getCompletedBookings()).append("** complétées). ");
            sb.append("Les dépenses s’élèvent à **").append(fmt(s.getTotalExpenses())).append(" RWF**, donc un bénéfice net de **").append(fmt(s.getNetProfit())).append(" RWF** et une marge de **").append(f1(s.getProfitMargin())).append("%**. ");
        } else {
            sb.append("Looking at this period’s numbers: this month you earned **").append(fmt(s.getTotalIncome())).append(" RWF** from **").append(s.getTotalBookings()).append("** booking(s) (**").append(s.getCompletedBookings()).append("** completed). ");
            sb.append("Expenses are **").append(fmt(s.getTotalExpenses())).append(" RWF**, so net profit is **").append(fmt(s.getNetProfit())).append(" RWF** with a **").append(f1(s.getProfitMargin())).append("%** margin. ");
        }
        if (prev != null && (prev.getTotalIncome() > 0 || prev.getTotalExpenses() > 0)) {
            double incomeChg = s.getTotalIncome() - prev.getTotalIncome();
            double pct = prev.getTotalIncome() > 0 ? (incomeChg / prev.getTotalIncome()) * 100 : 0;
            if (fr) {
                sb.append("Par rapport au mois dernier (revenu **").append(fmt(prev.getTotalIncome())).append(" RWF**), le revenu ");
                if (incomeChg > 0) sb.append("augmente de **").append(fmt(incomeChg)).append(" RWF** (+").append(f1(pct)).append("%). ");
                else if (incomeChg < 0) sb.append("baisse de **").append(fmt(-incomeChg)).append(" RWF** (").append(f1(pct)).append("%). ");
                else sb.append("reste stable. ");
            } else {
                sb.append("Compared to last month (revenue **").append(fmt(prev.getTotalIncome())).append(" RWF**), revenue ");
                if (incomeChg > 0) sb.append("is up **").append(fmt(incomeChg)).append(" RWF** (+").append(f1(pct)).append("%). ");
                else if (incomeChg < 0) sb.append("is down **").append(fmt(-incomeChg)).append(" RWF** (").append(f1(pct)).append("%). ");
                else sb.append("is flat. ");
            }
        }
        String topBranch = r.getTopBranch();
        if (topBranch != null && !topBranch.isBlank()) {
            if (fr) sb.append("La meilleure branche ce mois est **").append(topBranch).append("**. ");
            else sb.append("Your best-performing branch this month is **").append(topBranch).append("**. ");
        }
        String topCat = r.getTopExpenseCategory();
        if (topCat != null && !topCat.isBlank()) {
            double topAmt = topExpenseAmount(s.getCategoryBreakdown(), topCat);
            if (fr) sb.append("La plus grosse dépense par catégorie est **").append(topCat).append("** avec **").append(fmt(topAmt)).append(" RWF**. ");
            else sb.append("The largest expense category is **").append(topCat).append("** at **").append(fmt(topAmt)).append(" RWF**. ");
        }
        if (fr) {
            sb.append("Tendance : ").append(trendFr(r.getTrend())).append(" ; momentum : ").append(momentumFr(r.getMomentum())).append(". ");
            sb.append("Projection mois prochain : **").append(fmt(r.getProjectedNextMonthIncome())).append(" RWF**. ");
        } else {
            sb.append("Trend: ").append(trendEn(r.getTrend())).append("; momentum: ").append(momentumEn(r.getMomentum())).append(". ");
            sb.append("Next month projection: **").append(fmt(r.getProjectedNextMonthIncome())).append(" RWF**. ");
        }
        if (r.getUrgentActions() != null && !r.getUrgentActions().isEmpty()) {
            sb.append("\n\n");
            r.getUrgentActions().forEach(a -> sb.append("• ").append(a).append("\n"));
        }
        if (r.getQuickWins() != null && !r.getQuickWins().isEmpty()) {
            r.getQuickWins().forEach(w -> sb.append("• ").append(w).append("\n"));
        }
        return sb.toString().trim();
    }

    private double topExpenseAmount(List<Object[]> categoryBreakdown, String categoryName) {
        if (categoryBreakdown == null) return 0;
        for (Object[] row : categoryBreakdown) {
            if (row != null && row.length >= 2 && categoryName.equals(String.valueOf(row[0])))
                return toDouble(row[1]);
        }
        return 0;
    }

    private String buildExpenses(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        double targetRwf = r.getTopExpenseCategoryReductionTargetRwf();
        if (fr) {
            return String.format("""
                💸 **Analyse Détaillée des Dépenses**

                | Indicateur | Valeur |
                |---|---|
                | Total dépenses | **%s RWF** (%.1f%% du revenu) |
                | Catégorie principale | **%s** |
                | Objectif de réduction (cible) | **%s RWF** |

                %s

                **Répartition par catégorie :**
                %s

                **Recommandations de réduction :**
                %s

                **Conclusion :** Viser une réduction d’au moins **%s RWF** sur la catégorie **%s** pour améliorer la marge.
                """,
                fmt(s.getTotalExpenses()), r.getExpenseRatio(), r.getTopExpenseCategory(), fmt(targetRwf),
                alertBlock(r.getExpenseAlerts(), fr), categoryTable(s.getCategoryBreakdown(), fr),
                recsBlock(r.getStrategicRecommendations(), fr), fmt(targetRwf), r.getTopExpenseCategory()
            );
        } else {
            return String.format("""
                💸 **Detailed Expense Analysis**

                | Metric | Value |
                |---|---|
                | Total Expenses | **%s RWF** (%.1f%% of income) |
                | Top Category | **%s** |
                | Reduction target | **%s RWF** |

                %s

                **Category Breakdown:**
                %s

                **Cost Reduction Recommendations:**
                %s

                **Conclusion:** Aim to cut at least **%s RWF** in **%s** to improve margin.
                """,
                fmt(s.getTotalExpenses()), r.getExpenseRatio(), r.getTopExpenseCategory(), fmt(targetRwf),
                alertBlock(r.getExpenseAlerts(), fr), categoryTable(s.getCategoryBreakdown(), fr),
                recsBlock(r.getStrategicRecommendations(), fr), fmt(targetRwf), r.getTopExpenseCategory()
            );
        }
    }

    /** Health summary; if user asks why score is low, explain from real data (Prompt 3). */
    private String buildHealth(DeepReport r, LiveFinancialSnapshot s, boolean fr, String userMessage) {
        int score = r.getHealthScore();
        String gradeLabel = healthScoreGradeLabel(score, fr);
        String emoji = scoreEmoji(score);
        boolean askingWhyLow = userMessage != null && (
            userMessage.toLowerCase().matches(".*(why|pourquoi|why is|why are|pourquoi (est|sont)|why (is|are) (our|the) (health|score).*low|bas).*")
            || userMessage.toLowerCase().contains("why is our health score low")
            || userMessage.toLowerCase().contains("pourquoi notre score")
        );
        if (askingWhyLow && score < 60) {
            StringBuilder sb = new StringBuilder();
            if (fr) {
                sb.append("Votre score est à **").append(score).append("/100** (").append(gradeLabel).append(") principalement à cause des éléments suivants, basés sur vos données réelles. ");
            } else {
                sb.append("Your score is **").append(score).append("/100** (").append(gradeLabel).append(") mainly because of the following, based on your actual data. ");
            }
            if (s.getProfitMargin() < 30) {
                if (fr) sb.append("La marge bénéficiaire est basse (**").append(f1(s.getProfitMargin())).append("%**), ce qui tire le score vers le bas. ");
                else sb.append("Profit margin is low (**").append(f1(s.getProfitMargin())).append("%**), which pulls the score down. ");
            }
            if (s.getTotalIncome() > 0 && s.getPendingAmount() / s.getTotalIncome() > 0.2) {
                if (fr) sb.append("Le montant en attente (**").append(fmt(s.getPendingAmount())).append(" RWF**) représente un ratio important du revenu. ");
                else sb.append("Pending amount (**").append(fmt(s.getPendingAmount())).append(" RWF**) is a significant share of revenue. ");
            }
            if (s.getOverdueBookings() > 0) {
                double impact = r.getOverdueBookingsImpactRwf();
                if (fr) sb.append("Vous avez **").append(s.getOverdueBookings()).append("** réservation(s) en retard (impact estimé **").append(fmt(impact)).append(" RWF**). ");
                else sb.append("You have **").append(s.getOverdueBookings()).append("** overdue booking(s) (estimated impact **").append(fmt(impact)).append(" RWF**). ");
            }
            if ("DECLINING".equals(r.getTrend()) || "STRONG_DECLINE".equals(r.getTrend())) {
                if (fr) sb.append("La tendance revenus est en baisse, ce qui pénalise aussi le score. ");
                else sb.append("Revenue trend is declining, which also hurts the score. ");
            }
            if (r.getUrgentActions() != null && !r.getUrgentActions().isEmpty()) {
                if (fr) sb.append("En priorité : ");
                else sb.append("Priority actions: ");
                r.getUrgentActions().forEach(a -> sb.append(a).append(" "));
            }
            return sb.toString().trim();
        }
        if (fr) {
            return emoji + " Votre santé financière est à **" + score + "/100** (" + gradeLabel + "). " +
                "Marge **" + f1(s.getProfitMargin()) + "%**, trésorerie " + cashFlowFr(r.getCashFlowStatus()) + ", risque " + riskFr(r.getRiskLevel()) + ". " +
                (s.getOverdueBookings() > 0 ? "Vous avez " + s.getOverdueBookings() + " réservation(s) en retard. " : "") +
                (r.getUrgentActions() != null && !r.getUrgentActions().isEmpty() ? "Actions urgentes : " + String.join(" ; ", r.getUrgentActions()) : "");
        } else {
            return emoji + " Your financial health is **" + score + "/100** (" + gradeLabel + "). " +
                "Margin **" + f1(s.getProfitMargin()) + "%**, cash flow " + cashFlowEn(r.getCashFlowStatus()) + ", risk " + riskEn(r.getRiskLevel()) + ". " +
                (s.getOverdueBookings() > 0 ? "You have " + s.getOverdueBookings() + " overdue booking(s). " : "") +
                (r.getUrgentActions() != null && !r.getUrgentActions().isEmpty() ? "Urgent actions: " + String.join("; ", r.getUrgentActions()) : "");
        }
    }

    private String buildBookings(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        String overdueImpact = "";
        if (s.getOverdueBookings() > 0 && r.getOverdueBookingsImpactRwf() > 0) {
            overdueImpact = fr
                ? "\n**Impact financier des réservations en retard :** **" + fmt(r.getOverdueBookingsImpactRwf()) + " RWF** à risque.\n"
                : "\n**Financial impact of overdue bookings:** **" + fmt(r.getOverdueBookingsImpactRwf()) + " RWF** at risk.\n";
        }
        String conclusionFr = s.getOverdueBookings() > 0
            ? "Mettez à jour le statut des " + s.getOverdueBookings() + " réservation(s) en retard en priorité."
            : "Continuez à convertir les réservations en attente en confirmées.";
        String conclusionEn = s.getOverdueBookings() > 0
            ? "Update the status of the " + s.getOverdueBookings() + " overdue booking(s) as a priority."
            : "Keep converting pending bookings to confirmed.";
        if (fr) {
            return String.format("""
                📅 **Analyse des Réservations**

                | Statut | Nombre |
                |---|---|
                | Total | **%d** |
                | ✅ Confirmées | **%d** |
                | 🏁 Complétées | **%d** |
                | ⏳ En attente | **%d** |
                | ❌ Annulées | **%d** |
                | 🚨 En retard | **%d** |

                **Taux de complétion :** %.1f%%
                **Taux d'annulation :** %.1f%%
                **Revenu moyen/réservation :** **%s RWF**
                %s

                **Conclusion :** %s
                """,
                s.getTotalBookings(), s.getConfirmedBookings(), s.getCompletedBookings(),
                s.getPendingBookings(), s.getCancelledBookings(), s.getOverdueBookings(),
                r.getBookingCompletionRate(), r.getCancellationRate(), fmt(r.getAverageRevenuePerBooking()),
                overdueImpact,
                conclusionFr
            );
        } else {
            return String.format("""
                📅 **Booking Analysis**

                | Status | Count |
                |---|---|
                | Total | **%d** |
                | ✅ Confirmed | **%d** |
                | 🏁 Completed | **%d** |
                | ⏳ Pending | **%d** |
                | ❌ Cancelled | **%d** |
                | 🚨 Overdue | **%d** |

                **Completion Rate:** %.1f%%
                **Cancellation Rate:** %.1f%%
                **Average Revenue/Booking:** **%s RWF**
                %s

                **Conclusion:** %s
                """,
                s.getTotalBookings(), s.getConfirmedBookings(), s.getCompletedBookings(),
                s.getPendingBookings(), s.getCancelledBookings(), s.getOverdueBookings(),
                r.getBookingCompletionRate(), r.getCancellationRate(), fmt(r.getAverageRevenuePerBooking()),
                overdueImpact,
                conclusionEn
            );
        }
    }

    private String buildProjection(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        double projected = r.getProjectedNextMonthIncome();
        double optimistic = projected * 1.15;
        double pessimistic = projected * 0.85;
        if (fr) {
            return String.format("""
                🔮 **Projection Financière**

                **Mois prochain (estimation basée sur tendance historique) :**

                | Scénario | Revenu Projeté |
                |---|---|
                | 🟢 Optimiste (+15%%) | **%s RWF** |
                | ⚪ Base | **%s RWF** |
                | 🔴 Pessimiste (-15%%) | **%s RWF** |

                **Tendance actuelle :** %s
                **Momentum :** %s

                Pour atteindre le scénario optimiste :
                %s
                """,
                fmt(optimistic), fmt(projected), fmt(pessimistic),
                trendFr(r.getTrend()), momentumFr(r.getMomentum()),
                recsBlock(r.getStrategicRecommendations(), fr)
            );
        } else {
            return String.format("""
                🔮 **Financial Projection**

                **Next Month (estimated from historical trend):**

                | Scenario | Projected Income |
                |---|---|
                | 🟢 Optimistic (+15%%) | **%s RWF** |
                | ⚪ Base | **%s RWF** |
                | 🔴 Pessimistic (-15%%) | **%s RWF** |

                **Current Trend:** %s
                **Momentum:** %s

                To reach the optimistic scenario:
                %s
                """,
                fmt(optimistic), fmt(projected), fmt(pessimistic),
                trendEn(r.getTrend()), momentumEn(r.getMomentum()),
                recsBlock(r.getStrategicRecommendations(), fr)
            );
        }
    }

    private String buildWelcome(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        int score = r.getHealthScore();
        String gradeLabel = healthScoreGradeLabel(score, fr);
        String emoji = scoreEmoji(score);
        if (fr) {
            return String.format("""
                👋 **Bonjour ! Je suis le conseiller financier interne de Madeleine Group.**

                %s **Santé financière actuelle : %d/100** (%s)

                Je travaille directement avec votre base de données — mes analyses sont basées sur vos données réelles en temps réel.

                **Je peux analyser :**
                • 📊 Performance globale du mois/année
                • 💸 Dépenses et optimisations possibles
                • 📈 Tendances et projections
                • 💰 Paiements en attente
                • 📅 Réservations et taux de completion
                • ⚠️ Risques et alertes urgentes
                • 🔮 Projections financières
                • 🏆 Performance par branche

                %s

                Que souhaitez-vous analyser ?
                """,
                emoji, score, gradeLabel,
                r.getUrgentActions() != null && !r.getUrgentActions().isEmpty()
                    ? "⚠️ **" + r.getUrgentActions().size() + " action(s) urgente(s) détectée(s):**\n" + urgentBlock(r.getUrgentActions(), fr)
                    : "✅ Aucune action urgente requise."
            );
        } else {
            return String.format("""
                👋 **Hello! I'm Madeleine Group's internal financial advisor.**

                %s **Current Financial Health: %d/100** (%s)

                I work directly with your database — my analyses are based on your real-time actual data.

                **I can analyze:**
                • 📊 Overall monthly/yearly performance
                • 💸 Expenses and optimization opportunities
                • 📈 Trends and projections
                • 💰 Pending payments
                • 📅 Bookings and completion rates
                • ⚠️ Risks and urgent alerts
                • 🔮 Financial projections
                • 🏆 Branch performance

                %s

                What would you like to analyze?
                """,
                emoji, score, gradeLabel,
                r.getUrgentActions() != null && !r.getUrgentActions().isEmpty()
                    ? "⚠️ **" + r.getUrgentActions().size() + " urgent action(s) detected:**\n" + urgentBlock(r.getUrgentActions(), fr)
                    : "✅ No urgent actions required."
            );
        }
    }

    private String buildQuickWins(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        return (fr ? "⚡ **Gains Rapides Possibles**\n\n" : "⚡ **Available Quick Wins**\n\n")
            + quickWinsBlock(r.getQuickWins(), fr);
    }

    private String buildPending(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        double collect50 = s.getPendingAmount() * 0.5;
        if (fr) {
            return String.format("""
                💰 **Analyse des Paiements en Attente**

                Montant en attente : **%s RWF**
                Ratio sur revenu : **%.1f%%**
                Statut flux de trésorerie : %s

                **Si vous collectez 50%% aujourd'hui :** +%s RWF
                **Si vous collectez 100%% :** +%s RWF

                **Actions recommandées :**
                • Envoyez des rappels aux clients avec des soldes en attente
                • Exigez un acompte de 50%% pour toutes nouvelles réservations
                • Proposez un escompte de 5%% pour paiement immédiat
                • Fixez une date limite de paiement claire dans les contrats
                """,
                fmt(s.getPendingAmount()), r.getPendingRatio(), cashFlowFr(r.getCashFlowStatus()),
                fmt(collect50), fmt(s.getPendingAmount())
            );
        } else {
            return String.format("""
                💰 **Pending Payments Analysis**

                Pending Amount: **%s RWF**
                Ratio to Income: **%.1f%%**
                Cash Flow Status: %s

                **If you collect 50%% today:** +%s RWF
                **If you collect 100%%:** +%s RWF

                **Recommended Actions:**
                • Send reminders to clients with outstanding balances
                • Require 50%% deposit for all new bookings
                • Offer 5%% discount for immediate payment
                • Set clear payment deadlines in contracts
                """,
                fmt(s.getPendingAmount()), r.getPendingRatio(), cashFlowEn(r.getCashFlowStatus()),
                fmt(collect50), fmt(s.getPendingAmount())
            );
        }
    }

    private String buildRisks(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        if (fr) {
            return "⚠️ **Évaluation des Risques**\n\n" +
                "Niveau de risque global : " + riskFr(r.getRiskLevel()) + "\n\n" +
                alertBlock(r.getExpenseAlerts(), fr) + "\n" +
                urgentBlock(r.getUrgentActions(), fr);
        } else {
            return "⚠️ **Risk Assessment**\n\n" +
                "Overall Risk Level: " + riskEn(r.getRiskLevel()) + "\n\n" +
                alertBlock(r.getExpenseAlerts(), fr) + "\n" +
                urgentBlock(r.getUrgentActions(), fr);
        }
    }

    private String buildRevenue(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        if (fr) {
            return String.format("""
                📈 **Analyse et Croissance du Revenu**

                Revenu actuel : **%s RWF**
                Projection mois prochain : **%s RWF**
                Tendance : %s | Momentum : %s

                **Stratégies de croissance :**
                %s
                """,
                fmt(s.getTotalIncome()), fmt(r.getProjectedNextMonthIncome()),
                trendFr(r.getTrend()), momentumFr(r.getMomentum()),
                recsBlock(r.getStrategicRecommendations(), fr)
            );
        } else {
            return String.format("""
                📈 **Revenue Analysis & Growth**

                Current Revenue: **%s RWF**
                Next Month Projection: **%s RWF**
                Trend: %s | Momentum: %s

                **Growth Strategies:**
                %s
                """,
                fmt(s.getTotalIncome()), fmt(r.getProjectedNextMonthIncome()),
                trendEn(r.getTrend()), momentumEn(r.getMomentum()),
                recsBlock(r.getStrategicRecommendations(), fr)
            );
        }
    }

    private String buildProfit(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        double currentMargin = s.getProfitMargin();
        double gap = 60 - currentMargin;
        double toSave = r.getRwfToSaveForTargetMargin();
        double toEarn = r.getRwfToEarnForTargetMargin();
        if (fr) {
            return String.format("""
                💹 **Analyse de la Rentabilité**

                | Indicateur | Valeur |
                |---|---|
                | Bénéfice net | **%s RWF** |
                | Marge actuelle | **%.1f%%** (Grade **%s**) |
                | Objectif 60%% | Écart **%.1f%%** |

                %s

                **Pour atteindre 60%% de marge (chiffres exacts) :**
                • Réduire les dépenses de **%s RWF**, ou
                • Augmenter le revenu de **%s RWF**

                **Conclusion :** Choisissez une des deux actions ci-dessus et suivez les recommandations stratégiques pour y parvenir.
                """,
                fmt(s.getNetProfit()), currentMargin, r.getProfitMarginGrade(), gap,
                currentMargin >= 60 ? "✅ Vous avez déjà atteint l'objectif de 60%%." : "📌 Des améliorations sont nécessaires.",
                fmt(toSave), fmt(toEarn)
            );
        } else {
            return String.format("""
                💹 **Profitability Analysis**

                | Metric | Value |
                |---|---|
                | Net Profit | **%s RWF** |
                | Current Margin | **%.1f%%** (Grade **%s**) |
                | Target 60%% | Gap **%.1f%%** |

                %s

                **To reach 60%% margin (exact figures):**
                • Reduce expenses by **%s RWF**, or
                • Increase revenue by **%s RWF**

                **Conclusion:** Pick one of the two actions above and follow the strategic recommendations to get there.
                """,
                fmt(s.getNetProfit()), currentMargin, r.getProfitMarginGrade(), gap,
                currentMargin >= 60 ? "✅ You have already reached the 60%% target." : "📌 Improvements are needed.",
                fmt(toSave), fmt(toEarn)
            );
        }
    }

    private String buildClients(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        if (fr) {
            return String.format("""
                👥 **Analyse Clientèle**

                Clients totaux : **%d**
                Nouveaux ce mois : **%d**
                Revenu moyen par client : **%s RWF**
                Revenu moyen par réservation : **%s RWF**

                **Top clients par revenu :**
                %s
                """,
                s.getTotalClients(), s.getNewClientsThisPeriod(),
                fmt(r.getRevenuePerClient()), fmt(r.getAverageRevenuePerBooking()),
                topClientsTable(s.getTopClients(), fr)
            );
        } else {
            return String.format("""
                👥 **Client Analysis**

                Total Clients: **%d**
                New This Period: **%d**
                Average Revenue per Client: **%s RWF**
                Average Revenue per Booking: **%s RWF**

                **Top Clients by Revenue:**
                %s
                """,
                s.getTotalClients(), s.getNewClientsThisPeriod(),
                fmt(r.getRevenuePerClient()), fmt(r.getAverageRevenuePerBooking()),
                topClientsTable(s.getTopClients(), fr)
            );
        }
    }

    private String buildBranches(DeepReport r, LiveFinancialSnapshot s, boolean fr) {
        if (fr) {
            return String.format("""
                🏢 **Performance par Branche**

                Meilleure branche : **%s**
                Branche à améliorer : **%s**

                **Détail par branche :**
                %s
                """,
                r.getTopBranch(), r.getUnderperformingBranch(),
                branchTable(s.getBranchPerformance(), fr)
            );
        } else {
            return String.format("""
                🏢 **Branch Performance**

                Top Branch: **%s**
                Underperforming Branch: **%s**

                **Branch Breakdown:**
                %s
                """,
                r.getTopBranch(), r.getUnderperformingBranch(),
                branchTable(s.getBranchPerformance(), fr)
            );
        }
    }

    private String buildFull(DeepReport r, LiveFinancialSnapshot s, boolean fr, LiveFinancialSnapshot snapshotPrevious) {
        return buildPerformance(r, s, fr, snapshotPrevious);
    }

    private String urgentBlock(List<String> actions, boolean fr) {
        if (actions == null || actions.isEmpty())
            return fr ? "✅ Aucune action urgente." : "✅ No urgent actions.";
        StringBuilder sb = new StringBuilder();
        actions.forEach(a -> sb.append(a).append("\n"));
        return sb.toString();
    }

    private String quickWinsBlock(List<String> wins, boolean fr) {
        if (wins == null || wins.isEmpty())
            return fr ? "✅ Situation optimisée." : "✅ Situation optimized.";
        StringBuilder sb = new StringBuilder();
        wins.forEach(w -> sb.append(w).append("\n"));
        return sb.toString();
    }

    private String alertBlock(List<String> alerts, boolean fr) {
        if (alerts == null || alerts.isEmpty())
            return fr ? "✅ Aucune alerte de dépenses." : "✅ No expense alerts.";
        StringBuilder sb = new StringBuilder(fr ? "**⚠️ Alertes :**\n" : "**⚠️ Alerts:**\n");
        alerts.forEach(a -> sb.append("• ").append(a).append("\n"));
        return sb.toString();
    }

    private String recsBlock(List<String> recs, boolean fr) {
        if (recs == null || recs.isEmpty())
            return fr ? "✅ Continuez les bonnes pratiques." : "✅ Continue current best practices.";
        StringBuilder sb = new StringBuilder();
        recs.forEach(rec -> sb.append("• ").append(rec).append("\n"));
        return sb.toString();
    }

    private String categoryTable(List<Object[]> cats, boolean fr) {
        if (cats == null || cats.isEmpty())
            return fr ? "Aucune donnée." : "No data.";
        StringBuilder sb = new StringBuilder(fr ? "| Catégorie | Montant |\n|---|---|\n" : "| Category | Amount |\n|---|---|\n");
        cats.forEach(c -> sb.append("| ").append(c[0]).append(" | **").append(fmt(toDouble(c[1]))).append(" RWF** |\n"));
        return sb.toString();
    }

    private String branchTable(List<Object[]> branches, boolean fr) {
        if (branches == null || branches.isEmpty())
            return fr ? "Aucune donnée." : "No data.";
        StringBuilder sb = new StringBuilder(fr ? "| Branche | Revenu |\n|---|---|\n" : "| Branch | Revenue |\n|---|---|\n");
        branches.forEach(b -> sb.append("| ").append(b[0]).append(" | **").append(fmt(toDouble(b[1]))).append(" RWF** |\n"));
        return sb.toString();
    }

    private String topClientsTable(List<Object[]> clients, boolean fr) {
        if (clients == null || clients.isEmpty())
            return fr ? "Aucune donnée." : "No data.";
        StringBuilder sb = new StringBuilder(fr ? "| Client | Revenu |\n|---|---|\n" : "| Client | Revenue |\n|---|---|\n");
        clients.forEach(c -> sb.append("| ").append(c[0]).append(" | **").append(fmt(toDouble(c[1]))).append(" RWF** |\n"));
        return sb.toString();
    }

    private String overdueBanner(int overdue, boolean fr) {
        if (overdue == 0) return "";
        return fr
            ? "🚨 **" + overdue + " réservation(s) en retard** — action immédiate requise!"
            : "🚨 **" + overdue + " overdue booking(s)** — immediate action required!";
    }

    private String scoreEmoji(int score) {
        if (score >= 80) return "🟢";
        if (score >= 60) return "🟡";
        if (score >= 40) return "🟠";
        return "🔴";
    }

    private String riskFr(String r) {
        return switch (r != null ? r : "") {
            case "LOW" -> "🟢 Faible";
            case "MEDIUM" -> "🟡 Modéré";
            case "HIGH" -> "🟠 Élevé";
            default -> "🔴 Critique";
        };
    }

    private String riskEn(String r) {
        return switch (r != null ? r : "") {
            case "LOW" -> "🟢 Low";
            case "MEDIUM" -> "🟡 Medium";
            case "HIGH" -> "🟠 High";
            default -> "🔴 Critical";
        };
    }

    private String trendFr(String t) {
        return switch (t != null ? t : "") {
            case "STRONG_GROWTH" -> "📈 Forte croissance";
            case "GROWING" -> "📈 En croissance";
            case "STABLE" -> "➡️ Stable";
            case "DECLINING" -> "📉 En déclin";
            case "STRONG_DECLINE" -> "📉 Fort déclin";
            default -> "❓ Données insuffisantes";
        };
    }

    private String trendEn(String t) {
        return switch (t != null ? t : "") {
            case "STRONG_GROWTH" -> "📈 Strong growth";
            case "GROWING" -> "📈 Growing";
            case "STABLE" -> "➡️ Stable";
            case "DECLINING" -> "📉 Declining";
            case "STRONG_DECLINE" -> "📉 Strong decline";
            default -> "❓ Insufficient data";
        };
    }

    private String momentumFr(String m) {
        return switch (m != null ? m : "") {
            case "ACCELERATING" -> "🚀 Accélération";
            case "POSITIVE" -> "⬆️ Positif";
            case "NEUTRAL" -> "➡️ Neutre";
            case "SLOWING" -> "⬇️ Ralentissement";
            case "DECELERATING" -> "📉 Décélération";
            default -> "❓ Inconnu";
        };
    }

    private String momentumEn(String m) {
        return switch (m != null ? m : "") {
            case "ACCELERATING" -> "🚀 Accelerating";
            case "POSITIVE" -> "⬆️ Positive";
            case "NEUTRAL" -> "➡️ Neutral";
            case "SLOWING" -> "⬇️ Slowing";
            case "DECELERATING" -> "📉 Decelerating";
            default -> "❓ Unknown";
        };
    }

    private String cashFlowFr(String c) {
        return switch (c != null ? c : "") {
            case "STRONG" -> "🟢 Fort";
            case "HEALTHY" -> "🟢 Sain";
            case "MODERATE" -> "🟡 Modéré";
            case "TIGHT" -> "🟠 Tendu";
            default -> "🔴 Critique";
        };
    }

    private String cashFlowEn(String c) {
        return switch (c != null ? c : "") {
            case "STRONG" -> "🟢 Strong";
            case "HEALTHY" -> "🟢 Healthy";
            case "MODERATE" -> "🟡 Moderate";
            case "TIGHT" -> "🟠 Tight";
            default -> "🔴 Critical";
        };
    }

    private String marginGradeFr(String g) {
        return switch (g != null ? g : "") {
            case "A+", "A" -> "🟢 Excellent";
            case "B+", "B" -> "🟡 Bon";
            case "C" -> "🟠 Acceptable";
            default -> "🔴 À améliorer";
        };
    }

    private String marginGradeEn(String g) {
        return switch (g != null ? g : "") {
            case "A+", "A" -> "🟢 Excellent";
            case "B+", "B" -> "🟡 Good";
            case "C" -> "🟠 Fair";
            default -> "🔴 Needs improvement";
        };
    }

    private String fmt(double v) { return String.format("%,.0f", v); }

    private String f1(double v) { return String.format("%.1f", v); }

    private double toDouble(Object v) {
        if (v == null) return 0;
        try { return Double.parseDouble(v.toString()); }
        catch (Exception e) { return 0; }
    }
}
