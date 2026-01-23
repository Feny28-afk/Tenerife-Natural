package Tenerife.Natural.service;

import Tenerife.Natural.model.Usuario;
import Tenerife.Natural.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Buscamos al usuario en la base de datos usando el repositorio
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Convertimos nuestra entidad "Usuario" al objeto "UserDetails" que Spring Security entiende
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword()) // Importante: la contraseña en la DB debe estar cifrada con BCrypt
                .roles("USER") // Por ahora asignamos el rol de usuario por defecto
                .build();
    }
}