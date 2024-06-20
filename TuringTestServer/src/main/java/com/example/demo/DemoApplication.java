package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.demo.controller.GameController;
import com.example.demo.controller.MatchController;
import com.example.demo.interceptor.GameHandshakeInterceptor;
import com.example.demo.interceptor.MatchHandshakeInterceptor;

@SpringBootApplication
public class DemoApplication{
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Configuration
	@EnableWebSocket
	public class AppConfig implements WebSocketConfigurer,WebMvcConfigurer {
	
		@Autowired 
		private GameHandshakeInterceptor gamehandshakeinterceptor;
		@Autowired
		private GameController gamecontroller;

		@Autowired 
		private MatchHandshakeInterceptor matchhandshakeinterceptor;
		@Autowired
		private MatchController matchcontroller;
	
		@Override
		public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
			registry.addHandler(gamecontroller,"/game").
					addInterceptors(gamehandshakeinterceptor);
			registry.addHandler(matchcontroller,"/match")
					.addInterceptors(matchhandshakeinterceptor);
		}
	}

	// @Bean
    // WebSocketConfigurer createWebSocketConfigurer(@Autowired ChatHandler chatHandler, @Autowired ChatHandshakeInterceptor chatInterceptor) {
    //     return new WebSocketConfigurer() {
    //         @Override
    //         public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    //             // 把URL与指定的WebSocketHandler关联，可关联多个:
    //             registry.addHandler(chatHandler, "/chat").addInterceptors(chatInterceptor);
    //         }
    //     };
    // }

}
