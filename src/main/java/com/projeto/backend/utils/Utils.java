package com.projeto.backend.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Utils {

	public static String gerarHashSenha(final String senha) {
		BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder(16);
		return bCrypt.encode(senha);
	}

}