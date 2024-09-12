package com.example.techtask.service.impl;
import com.example.techtask.model.Order;
import com.example.techtask.model.User;
import com.example.techtask.model.enumiration.UserStatus;
import com.example.techtask.service.OrderService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.*;


@Service
@NoArgsConstructor
public class OrderServiceImpl implements OrderService {
	private List<Order> orders;
	private List<User> users;
	@PersistenceContext
	private EntityManager entityManager;
	@PostConstruct
	public void init() {
		TypedQuery<Order> orderQuery = entityManager.createQuery("SELECT o FROM Order o", Order.class);
		orders = orderQuery.getResultList();
		
		TypedQuery<User> userQuery = entityManager.createQuery("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.orders", User.class);
		users = userQuery.getResultList();
	}
	public Order findOrder(){
		return orders.stream()
				.filter(order -> order.getQuantity() > 1)
				.reduce((order1, order2) -> order1.getCreatedAt().isBefore(order2.getCreatedAt())?order2:order1)
				.orElse(null);
				
	};
	public List<Order> findOrders(){
		Map<Integer,User> activeUser = new HashMap<>();
		List<Order> activeOrders = new ArrayList<>();
		users.forEach(user->{
			if(user.getUserStatus() == UserStatus.ACTIVE)
				activeUser.put(user.getId(),user);
		});
		orders.forEach(order->{
			if(activeUser.containsKey(order.getUserId())){
				activeOrders.add(order);
			}
		});
		return activeOrders.stream().sorted(Comparator.comparing(Order::getCreatedAt).reversed()).toList();
	};
}
