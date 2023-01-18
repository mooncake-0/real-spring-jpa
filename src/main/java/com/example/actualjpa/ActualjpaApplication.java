package com.example.actualjpa;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SpringBootApplication
public class ActualjpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ActualjpaApplication.class, args);
	}

	@PersistenceContext
	private EntityManager entityManager;

	@Bean
	public JPAQueryFactory jpaQueryFactory() {
		return new JPAQueryFactory(entityManager);
	}

	/*
	 JSON IGNORE 을 걸어도, 프록시 객체를 Response 에 실장할 수 없다는 에러가  뜸.
	 >> 그래서 이와 같이설정해 두면, Json Ignore 을 건 연관관계 객체들은 조회하고 변환하지 않은채로 전달할 수 있게 됨
	 */
	/*@Bean
	Hibernate5Module hibernate5Module() {
		return new Hibernate5Module();
	}*/

	/*
	 이렇게 해두면, Lazy 로딩한 녀석들에 대해서 무한 Loop 을 돌지 않고 필요 객체들만 호출하여
	 전달할 수 있게 됨. (서로가 서로를 호출하는걸 막아줌)
	 연관관계 되어 있는 모~~ 든 애들 다 쿼리 나감. 심지어 조인도 아니로 Lazy 로 나감
	 >> 그렇기 때문에 이렇게 하면 안됨.
	 */
	/*@Bean
	Hibernate5Module hibernate5Module() {
		Hibernate5Module hibernate5Module = new Hibernate5Module();
//		hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);

		return hibernate5Module;
	}*/

	// 그냥 이렇게 하면 안됨 ㅇㅇ
	// 이게 다 Entity 노출해서 그런거임
}
