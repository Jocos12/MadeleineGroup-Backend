package rw.madeleinegroup.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.madeleinegroup.entity.*;
import rw.madeleinegroup.repository.BranchRepository;
import rw.madeleinegroup.repository.DepartmentRepository;
import rw.madeleinegroup.repository.PackageItemRepository;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final PackageItemRepository packageItemRepository;
    private final BranchRepository branchRepository;

    public DataInitializer(DepartmentRepository departmentRepository, PackageItemRepository packageItemRepository,
                          BranchRepository branchRepository) {
        this.departmentRepository = departmentRepository;
        this.packageItemRepository = packageItemRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDepartmentsAndPackages();
    }

    private void seedDepartmentsAndPackages() {
        Department decor = getOrCreateDept("DECOR_COLLECTION", "Décor and Collection", "Event decoration and styling");
        Department catering = getOrCreateDept("CATERING", "Catering", "Culinary services");
        Department studio = getOrCreateDept("STUDIO", "Studio", "Photography and videography");
        Department sound = getOrCreateDept("SOUND_BAND", "Sound and Band", "Sound systems, live bands, MC services");
        Department protocol = getOrCreateDept("PROTOCOL_SERVICES", "Protocol and Services", "Event coordination and hospitality");
        Department carRental = getOrCreateDept("CAR_RENTAL", "Car Rental", "Vehicle rental for events");
        Department madeleineGarden = getOrCreateDept("MADELEINE_GARDEN", "Madeleine Garden", "Venue rental - main hall, outdoor spaces, guest house");

        savePackages(madeleineGarden, List.of(
                pkg("Full Event Package", "Main hall, outdoor spaces, 500 chairs, 50 tables, basic decoration, sound system, buffet tents, generator, guest house, parking 300+ vehicles", 2_500_000, PricingType.FIXED, null, null),
                pkg("House Only", "Guest house only (2 bedrooms, bathroom, toilets) - 50,000 RWF", 50_000, PricingType.FIXED, null, null)
        ));

        savePackages(decor, List.of(
                pkg("Standard", "Standard décor package", 800_000, PricingType.FIXED, null, null),
                pkg("Premium", "Premium décor package", 1_200_000, PricingType.FIXED, null, null),
                pkg("Golden", "Golden décor package", 1_500_000, PricingType.FIXED, null, null),
                pkg("Diamond", "Diamond décor package", 2_000_000, PricingType.FIXED, null, null),
                pkg("Platinum", "Platinum décor package", 2_500_000, PricingType.FIXED, null, null)
        ));

        savePackages(catering, List.of(
                pkg("Standard", "One type of meat with sides and fruits", 4_000, PricingType.PER_GUEST, null, null),
                pkg("Classic", "Two types of meat", 6_000, PricingType.PER_GUEST, null, null),
                pkg("Premium", "Three types of meat", 8_000, PricingType.PER_GUEST, null, null),
                pkg("Luxury", "Three types of meat, full pastry, all fruits, elegant buffet, event cake", 10_000, PricingType.PER_GUEST, null, null)
        ));

        savePackages(studio, List.of(
                pkg("Silver", "Silver photography and videography", 800_000, PricingType.FIXED, null, null),
                pkg("Golden", "Golden photography and videography", 1_200_000, PricingType.FIXED, null, null),
                pkg("Diamond", "Diamond photography and videography", 1_500_000, PricingType.FIXED, null, null),
                pkg("Platinum", "Platinum photography and videography", 2_000_000, PricingType.FIXED, null, null)
        ));

        savePackages(sound, List.of(
                pkg("Sound System Standard", "Sound system 300-500k range", 300_000, PricingType.FIXED, null, null),
                pkg("Sound System Premium", "Premium sound system", 500_000, PricingType.FIXED, null, null),
                pkg("Live Band Standard", "Live band 1.5-2M range", 1_500_000, PricingType.FIXED, null, null),
                pkg("Live Band Premium", "Premium live band", 2_000_000, PricingType.FIXED, null, null),
                pkg("MC Full Day", "MC full day (Dot and Reception)", 400_000, PricingType.FIXED, null, null),
                pkg("MC One Session", "MC one session only", 300_000, PricingType.FIXED, null, null),
                pkg("Gusohora Umugeni", "Traditional ceremony performance", 500_000, PricingType.FIXED, null, null),
                pkg("Itorero and Traditional Dancers", "Itorero and dancers", 800_000, PricingType.FIXED, null, null),
                pkg("Kuvugira Inka", "Traditional ceremony", 50_000, PricingType.FIXED, null, null)
        ));

        savePackages(protocol, List.of(
                pkg("Standard Full Day", "Up to 400 guests, full day", 250_000, PricingType.FIXED, null, 400),
                pkg("Standard Half Day", "Up to 400 guests, half day", 150_000, PricingType.FIXED, null, 400),
                pkg("Premium Full Day", "Up to 500 guests, full day", 300_000, PricingType.FIXED, null, 500),
                pkg("Premium Half Day", "Up to 500 guests, half day", 200_000, PricingType.FIXED, null, 500),
                pkg("Luxe Full Day", "500+ guests, full day", 500_000, PricingType.FIXED, 500, null),
                pkg("Luxe Half Day", "500+ guests, half day", 350_000, PricingType.FIXED, 500, null)
        ));

        savePackages(carRental, List.of(
                pkg("Toyota RAV4 2015 with driver", "With driver", 100_000, PricingType.FIXED, null, null),
                pkg("Toyota RAV4 2015 without driver", "Self-drive", 75_000, PricingType.FIXED, null, null),
                pkg("Toyota RAV4 2024 with driver", "With driver", 120_000, PricingType.FIXED, null, null),
                pkg("Toyota RAV4 2024 without driver", "Self-drive", 80_000, PricingType.FIXED, null, null),
                pkg("Land Cruiser Prado 2015 with driver", "With driver", 150_000, PricingType.FIXED, null, null),
                pkg("Land Cruiser Prado 2015 without driver", "Self-drive", 100_000, PricingType.FIXED, null, null),
                pkg("Land Cruiser Prado 2023 with driver", "With driver", 250_000, PricingType.FIXED, null, null),
                pkg("Land Cruiser Prado 2023 without driver", "Self-drive", 200_000, PricingType.FIXED, null, null),
                pkg("Land Cruiser V8 with driver", "With driver", 300_000, PricingType.FIXED, null, null),
                pkg("Land Cruiser V8 without driver", "Self-drive", 250_000, PricingType.FIXED, null, null),
                pkg("Mercedes-Benz SUV with driver", "With driver", 350_000, PricingType.FIXED, null, null),
                pkg("Mercedes-Benz SUV without driver", "Self-drive", 300_000, PricingType.FIXED, null, null),
                pkg("Mercedes-Benz G-Class with driver", "With driver", 400_000, PricingType.FIXED, null, null),
                pkg("Mercedes-Benz G-Class without driver", "Self-drive", 350_000, PricingType.FIXED, null, null),
                pkg("Coaster Bus", "29 seats, full day round trip", 350_000, PricingType.FIXED, null, null)
        ));
    }

    private Department getOrCreateDept(String code, String name, String desc) {
        return departmentRepository.findByCode(code)
                .orElseGet(() -> {
                    Department d = new Department();
                    d.setCode(code);
                    d.setName(name);
                    d.setDescription(desc);
                    return departmentRepository.save(d);
                });
    }

    private PackageData pkg(String name, String desc, long price, PricingType type, Integer min, Integer max) {
        return new PackageData(name, desc, BigDecimal.valueOf(price), type, min, max);
    }

    /** Maps department codes to branch codes for package assignment */
    private Branch getBranchForDepartment(String deptCode) {
        return switch (deptCode) {
            case "MADELEINE_GARDEN", "CAR_RENTAL" -> branchRepository.findByCode("GARDEN").orElse(null);
            case "DECOR_COLLECTION" -> branchRepository.findByCode("DECOR").orElse(null);
            case "COLLECTION" -> branchRepository.findByCode("COLLECTION").orElse(null);
            case "CATERING" -> branchRepository.findByCode("CATERING").orElse(null);
            case "STUDIO", "SOUND_BAND" -> branchRepository.findByCode("STUDIO").orElse(null);
            case "PROTOCOL_SERVICES" -> branchRepository.findByCode("PROTOCOL").orElse(null);
            default -> branchRepository.findAll().stream().findFirst().orElse(null);
        };
    }

    private void savePackages(Department dept, List<PackageData> data) {
        Department saved = departmentRepository.findByCode(dept.getCode()).orElseGet(() -> departmentRepository.save(dept));
        Branch branch = getBranchForDepartment(dept.getCode());
        if (branch == null) branch = branchRepository.findAll().stream().findFirst().orElse(null);
        final Branch targetBranch = branch;
        for (PackageData pd : data) {
            if (packageItemRepository.findByDepartment(saved).stream().noneMatch(p -> p.getName().equals(pd.name))) {
                PackageItem p = PackageItem.builder()
                        .branch(targetBranch)
                        .department(saved)
                        .name(pd.name)
                        .description(pd.description)
                        .price(pd.price)
                        .pricingType(pd.type)
                        .minGuests(pd.minGuests)
                        .maxGuests(pd.maxGuests)
                        .build();
                packageItemRepository.save(p);
            } else {
                var existing = packageItemRepository.findByDepartment(saved).stream()
                        .filter(p -> p.getName().equals(pd.name)).findFirst();
                existing.ifPresent(pkg -> {
                    if (targetBranch != null && (pkg.getBranch() == null || !targetBranch.getId().equals(pkg.getBranch().getId()))) {
                        pkg.setBranch(targetBranch);
                        packageItemRepository.save(pkg);
                    }
                });
            }
        }
    }

    private record PackageData(String name, String description, BigDecimal price, PricingType type, Integer minGuests, Integer maxGuests) {}
}
