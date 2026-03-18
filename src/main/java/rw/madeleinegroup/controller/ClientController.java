package rw.madeleinegroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.madeleinegroup.dto.ClientResponse;
import rw.madeleinegroup.entity.Client;
import rw.madeleinegroup.service.ClientService;
import rw.madeleinegroup.service.CustomUserDetailsService;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<?> createClient(@RequestBody Client client,
                                         @AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        Client saved = clientService.save(client, principal.getId());
        return ResponseEntity.ok(saved.getId());
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients(@RequestParam(required = false) Long branchId) {
        return ResponseEntity.ok(clientService.findAllAsResponse(branchId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.findByIdAsResponse(id));
    }

    @GetMapping("/me-or-create")
    public ResponseEntity<?> meOrCreate(@AuthenticationPrincipal CustomUserDetailsService.UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        var client = clientService.getOrCreateForUser(principal.getId());
        return ResponseEntity.ok(java.util.Map.of(
                "id", client.getId(),
                "fullName", client.getFullName(),
                "email", client.getEmail(),
                "phone", client.getPhone() != null ? client.getPhone() : ""
        ));
    }
}
