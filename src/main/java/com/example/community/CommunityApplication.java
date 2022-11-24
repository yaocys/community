package com.example.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * @author yaosu
 */
@SpringBootApplication
public class CommunityApplication {

	/**
	 * 解决Redis和ElasticSearch底层同时使用netty导致地冲突问题
	 * Netty4Utils.setAvailableProcessors();
	 */
	@PostConstruct
	public void init(){
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
