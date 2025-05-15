package com.example.app.config;

import com.example.app.util.JwtServiceImpl;
import io.jsonwebtoken.ExpiredJwtException; // Import pentru a prinde specific excepția de token expirat
import io.jsonwebtoken.JwtException; // Import pentru excepții JWT mai generale
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor; // Sau @RequiredArgsConstructor dacă preferi injectarea prin constructor a final fields
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import pentru excepția specifică
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@AllArgsConstructor // Asigură-te că ai dependențele injectate corect prin constructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtServiceImpl jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String requestURI = request.getRequestURI(); // Logăm URI-ul pentru context
        System.out.println("[JWT_FILTER] Processing request for: " + requestURI);

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JWT_FILTER] No JWT Token found in Authorization header or header doesn't start with Bearer. Passing to next filter for: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("[JWT_FILTER] Authorization header found: " + authHeader.substring(0, Math.min(authHeader.length(), 15)) + "..."); // Logăm o parte din header

        try {
            final String jwt = authHeader.substring(7);
            System.out.println("[JWT_FILTER] Extracted JWT (first 10 chars): " + jwt.substring(0, Math.min(jwt.length(), 10)) + "...");

            final String userEmail; // Sau username, depinde ce stochezi ca subject
            try {
                userEmail = jwtService.extractUsername(jwt);
                System.out.println("[JWT_FILTER] Username/Email extracted from JWT: " + userEmail);
            } catch (ExpiredJwtException eje) {
                System.err.println("[JWT_FILTER] JWT Token has expired for URI " + requestURI + ". Message: " + eje.getMessage());
                // handlerExceptionResolver.resolveException(request, response, null, eje); // Poți lăsa resolver-ul să gestioneze sau să trimiți un 401 specific
                // filterChain.doFilter(request, response); // Permite continuarea lanțului pentru ca Spring Security să dea 401/403
                // return; // Sau oprește aici dacă vrei un control mai fin
                throw eje; // Aruncă excepția pentru a fi prinsă de catch-ul general de mai jos sau de Spring Security
            } catch (JwtException je) { // Prinde alte excepții legate de JWT (SignatureException, MalformedJwtException etc.)
                System.err.println("[JWT_FILTER] Invalid JWT Token (parsing/signature issue) for URI " + requestURI + ". Message: " + je.getMessage());
                throw je;
            }


            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("[JWT_FILTER] Current SecurityContext Authentication: " + (authentication == null ? "null" : authentication.getName()));


            if (userEmail != null && authentication == null) { // Doar dacă nu există deja o autentificare în context
                System.out.println("[JWT_FILTER] Attempting to load UserDetails for: " + userEmail);
                UserDetails userDetails;
                try {
                    userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                    System.out.println("[JWT_FILTER] UserDetails loaded for '" + userDetails.getUsername() + "'. Enabled: " + userDetails.isEnabled() + ", Authorities: " + userDetails.getAuthorities());
                } catch (UsernameNotFoundException unfe) {
                    System.err.println("[JWT_FILTER] User not found by UserDetailsService for username/email: " + userEmail + ". Message: " + unfe.getMessage());
                    throw unfe; // Aruncă excepția
                }


                System.out.println("[JWT_FILTER] Validating token with JwtServiceImpl...");
                boolean isTokenValid;
                try {
                    isTokenValid = jwtService.isTokenValid(jwt, userDetails);
                } catch (Exception e) {
                    System.err.println("[JWT_FILTER] Exception during jwtService.isTokenValid: " + e.getMessage());
                    e.printStackTrace();
                    isTokenValid = false; // Consideră token-ul invalid în caz de eroare la validare
                }


                if (isTokenValid) {
                    System.out.println("[JWT_FILTER] Token IS VALID. Setting new Authentication in SecurityContext.");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials (parola) nu sunt necesare aici, token-ul e dovada
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("[JWT_FILTER] User '" + userDetails.getUsername() + "' authenticated successfully in SecurityContext for URI: " + requestURI);
                } else {
                    System.out.println("[JWT_FILTER] Token IS INVALID according to jwtService.isTokenValid for user: '" + userDetails.getUsername() + "' and URI: " + requestURI);
                    // Nu seta nimic în SecurityContext, lasă Spring Security să refuze accesul (va da 403 dacă ajunge la un endpoint protejat fără autentificare)
                }
            } else if (userEmail == null) {
                System.out.println("[JWT_FILTER] Username/Email extracted from token is null. Cannot proceed with authentication for URI: " + requestURI);
            } else { // authentication != null
                System.out.println("[JWT_FILTER] Authentication already present in SecurityContext for user '" + authentication.getName() + "'. Skipping token validation for URI: " + requestURI);
            }

            filterChain.doFilter(request, response); // Pasează request-ul mai departe în lanțul de filtre

        } catch (Exception exception) {
            // Acest catch prinde excepțiile aruncate mai sus (ExpiredJwtException, JwtException, UsernameNotFoundException)
            // sau orice altă excepție neașteptată.
            System.err.println("[JWT_FILTER] EXCEPTION caught in JWT authentication filter for URI " + requestURI + ". Message: " + exception.getMessage());
            exception.printStackTrace(); // FOARTE IMPORTANT PENTRU DEBUG! Afișează stack trace-ul complet.
            // Deleagă gestionarea excepției la handlerExceptionResolver.
            // Acesta poate converti excepția într-un răspuns HTTP corespunzător (ex: 401, 403, 500).
            this.handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }
}