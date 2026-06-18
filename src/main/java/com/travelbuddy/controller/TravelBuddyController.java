package com.travelbuddy.controller;

import com.travelbuddy.dto.AeroportoDTO;
import com.travelbuddy.dto.GuardarViagemDTO;
import com.travelbuddy.dto.SearchRequestDTO;
import com.travelbuddy.dto.TripResponseDTO;
import com.travelbuddy.dto.VooDTO;
import com.travelbuddy.model.Viagem;
import com.travelbuddy.service.AeroportoService;
import com.travelbuddy.service.FlightService;
import com.travelbuddy.service.ResultadoCache;
import com.travelbuddy.service.TripSearchService;
import com.travelbuddy.service.ViagemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TravelBuddyController {

    private final TripSearchService tripSearchService;
    private final AeroportoService aeroportoService;
    private final ResultadoCache resultadoCache;
    private final FlightService flightService;
    private final ViagemService viagemService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/search")
    public String search(Model model, @AuthenticationPrincipal OAuth2User user) {
        model.addAttribute("userName", user.getAttribute("name"));
        model.addAttribute("userPhoto", user.getAttribute("picture"));
        model.addAttribute("searchRequest", new SearchRequestDTO());
        return "search";
    }

    @PostMapping("/pesquisar")
    public String pesquisar(@ModelAttribute SearchRequestDTO request,
                            RedirectAttributes ra,
                            @AuthenticationPrincipal OAuth2User user) {
        log.info("Pesquisa iniciada: cidade={}, {} → {}",
                request.getCidade(), request.getAeroportoOrigem(), request.getAeroportoDestino());

        String id = UUID.randomUUID().toString();
        CompletableFuture<TripResponseDTO> future = CompletableFuture.supplyAsync(
                () -> tripSearchService.pesquisar(request));
        resultadoCache.guardar(id, new ResultadoCache.Entrada(future, request.getCidade()));

        ra.addAttribute("id", id);
        return "redirect:/resultados";
    }

    @GetMapping("/api/status")
    @ResponseBody
    public Map<String, Object> status(@RequestParam String id) {
        ResultadoCache.Entrada entrada = resultadoCache.obter(id);
        if (entrada == null) {
            return Map.of("pronto", true, "erro", "Pesquisa não encontrada.");
        }
        if (entrada.future().isCompletedExceptionally()) {
            return Map.of("pronto", true, "erro", "Ocorreu um erro durante a pesquisa.");
        }
        return Map.of("pronto", entrada.future().isDone());
    }

    @GetMapping("/resultados")
    public String resultados(@RequestParam String id,
                             Model model,
                             @AuthenticationPrincipal OAuth2User user) {
        model.addAttribute("userName", user.getAttribute("name"));
        model.addAttribute("userPhoto", user.getAttribute("picture"));

        ResultadoCache.Entrada entrada = resultadoCache.obter(id);
        if (entrada == null) {
            return "redirect:/search";
        }
        if (!entrada.future().isDone()) {
            model.addAttribute("pesquisaId", id);
            model.addAttribute("cidade", entrada.cidade());
            return "resultados";
        }

        try {
            TripResponseDTO resultado = entrada.future().get();
            model.addAttribute("resultado", resultado);
        } catch (Exception e) {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            log.error("Erro ao obter resultado da pesquisa {}", id, causa);
            model.addAttribute("erro", "Não foi possível completar a pesquisa: " + causa.getMessage());
        }

        return "resultados";
    }

    @GetMapping("/api/aeroportos")
    @ResponseBody
    public List<AeroportoDTO> pesquisarAeroportos(@RequestParam(defaultValue = "") String q) {
        return aeroportoService.pesquisar(q);
    }

    @GetMapping("/api/voos/regresso")
    @ResponseBody
    public List<VooDTO> voosRegresso(@RequestParam String token) {
        return flightService.pesquisarVoosRegresso(token);
    }

    @PostMapping("/api/viagens")
    @ResponseBody
    public Map<String, Object> guardarViagem(@RequestBody GuardarViagemDTO dto,
                                             @AuthenticationPrincipal OAuth2User user) {
        String email = user.getAttribute("email");
        Viagem v = viagemService.guardar(email, dto);
        return Map.of("id", v.getId(), "mensagem", "Viagem guardada com sucesso!");
    }

    @GetMapping("/viagens")
    public String minhasViagens(Model model, @AuthenticationPrincipal OAuth2User user) {
        model.addAttribute("userName", user.getAttribute("name"));
        model.addAttribute("userPhoto", user.getAttribute("picture"));
        model.addAttribute("viagens", viagemService.listar(user.getAttribute("email")));
        return "viagens";
    }

    @DeleteMapping("/api/viagens/{id}")
    @ResponseBody
    public Map<String, String> apagarViagem(@PathVariable Long id,
                                            @AuthenticationPrincipal OAuth2User user) {
        viagemService.apagar(id, user.getAttribute("email"));
        return Map.of("mensagem", "Viagem apagada.");
    }
}
