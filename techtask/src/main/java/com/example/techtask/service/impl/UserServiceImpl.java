package com.example.techtask.service.impl;

import com.example.techtask.model.Order;
import com.example.techtask.model.User;
import com.example.techtask.model.enumiration.OrderStatus;
import com.example.techtask.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@NoArgsConstructor
public class UserServiceImpl implements UserService {
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
	
	
	public User findUser(){
		Map<Integer, Double> userTotals = new HashMap<>();
		
		for (Order order : orders) {
			if (order.getCreatedAt().getYear() == 2003) {
				userTotals.merge(order.getUserId(), order.getPrice() * order.getQuantity(), Double::sum);
			}
		}
		
		return users.stream()
				.filter(user -> userTotals.containsKey(user.getId()))
				.reduce((user1, user2) -> {
					double total1 = userTotals.get(user1.getId());
					double total2 = userTotals.get(user2.getId());
					return total1 > total2 ? user1 : user2;
				})
				.orElse(null);
	}
	
	public List<User> findUsers(){
		Map<Integer,Order>paidOrders = new HashMap<>();
		orders.forEach(order -> {
			if(order.getCreatedAt().getYear()==2010 && order.getOrderStatus() == OrderStatus.PAID){
			 	paidOrders.put(order.getUserId(),order);
			}
		});
		
		List<User> paidUsers = new ArrayList<>();
		users.forEach(user -> {
			if(paidOrders.containsKey(user.getId())){
				paidUsers.add(user);
			}
		});
		return paidUsers;
	};
	
}
