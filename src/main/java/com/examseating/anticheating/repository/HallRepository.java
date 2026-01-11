package com.examseating.anticheating.repository;

import com.examseating.anticheating.model.ExamHall;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class HallRepository {
    
    private final ConcurrentHashMap<String, ExamHall> halls = new ConcurrentHashMap<>();
    
    public ExamHall save(ExamHall hall) {
        halls.put(hall.getHallId(), hall);
        return hall;
    }
    
    public ExamHall findById(String hallId) {
        return halls.get(hallId);
    }
    
    public List<ExamHall> findAll() {
        return halls.values().stream().toList();
    }
    
    public void delete(String hallId) {
        halls.remove(hallId);
    }
    
    public boolean existsById(String hallId) {
        return halls.containsKey(hallId);
    }
    
    public int count() {
        return halls.size();
    }
}