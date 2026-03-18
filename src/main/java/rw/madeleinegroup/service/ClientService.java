package rw.madeleinegroup.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import rw.madeleinegroup.dto.ClientResponse;
import rw.madeleinegroup.entity.Branch;
import rw.madeleinegroup.entity.Client;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.exception.ResourceNotFoundException;
import rw.madeleinegroup.repository.BranchRepository;
import rw.madeleinegroup.repository.ClientRepository;
import rw.madeleinegroup.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;

    public ClientService(ClientRepository clientRepository, BranchRepository branchRepository,
                         UserRepository userRepository, TransactionTemplate transactionTemplate) {
        this.clientRepository = clientRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public Client createClient(String fullName, String email, String phone, String address, String notes,
                               Long branchId, String managerEmail) {
        if (clientRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Client with email " + email + " already exists");
        }
        User createdBy = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Branch branch = branchId != null ? branchRepository.findById(branchId)
                .orElse(null) : null;

        Client client = Client.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .address(address)
                .notes(notes)
                .createdBy(createdBy)
                .branch(branch)
                .build();
        return clientRepository.save(client);
    }

    public Client updateClient(Long id, String fullName, String phone, String address, String notes, String profilePhotoUrl, Long branchId) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        if (fullName != null) client.setFullName(fullName);
        if (phone != null) client.setPhone(phone);
        if (address != null) client.setAddress(address);
        if (notes != null) client.setNotes(notes);
        if (profilePhotoUrl != null) client.setProfilePhotoUrl(profilePhotoUrl);
        if (branchId != null) {
            Branch branch = branchRepository.findById(branchId).orElse(null);
            client.setBranch(branch);
        } else {
            client.setBranch(null);
        }
        return clientRepository.save(client);
    }

    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public List<Client> getClientsByBranch(Long branchId) {
        return clientRepository.findByBranchId(branchId);
    }

    public Client save(Client client, Long createdById) {
        if (client.getEmail() != null && clientRepository.existsByEmail(client.getEmail()) && (client.getId() == null || !clientRepository.findById(client.getId()).orElseThrow().getEmail().equals(client.getEmail()))) {
            throw new IllegalArgumentException("Client with email " + client.getEmail() + " already exists");
        }
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        client.setCreatedBy(createdBy);
        return clientRepository.save(client);
    }

    public List<Client> findAll(Long branchId) {
        return branchId != null ? clientRepository.findByBranchId(branchId) : clientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> findAllAsResponse(Long branchId) {
        List<Client> clients = findAll(branchId);
        return clients.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientResponse findByIdAsResponse(Long id) {
        return toResponse(findById(id));
    }

    public ClientResponse toResponse(Client c) {
        Long branchId = c.getBranch() != null ? c.getBranch().getId() : null;
        String branchName = c.getBranch() != null ? c.getBranch().getName() : null;
        return ClientResponse.builder()
                .id(c.getId())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .address(c.getAddress())
                .notes(c.getNotes())
                .profilePhotoUrl(c.getProfilePhotoUrl())
                .branchId(branchId)
                .branchName(branchName)
                .createdAt(c.getCreatedAt())
                .build();
    }

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
        clientRepository.delete(client);
    }

    public Client getOrCreateForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return clientRepository.findByEmail(user.getEmail())
                .orElseGet(() -> {
                    try {
                        Client c = Client.builder()
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .phone(user.getPhone())
                                .createdBy(user)
                                .build();
                        return clientRepository.save(c);
                    } catch (DataIntegrityViolationException e) {
                        Client existing = transactionTemplate.execute(status ->
                                clientRepository.findByEmail(user.getEmail()).orElse(null));
                        if (existing != null) return existing;
                        throw new IllegalStateException("Client with this email already exists. Please try again.");
                    }
                });
    }
}
