package com.linkedinlearning.reactivespring.services;

import com.linkedinlearning.reactivespring.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReservationServiceImp implements ReservationService {

    private final ReactiveMongoOperations reactiveMongoOperations;

    @Autowired
    public ReservationServiceImp(ReactiveMongoOperations reactiveMongoOperations) {
        this.reactiveMongoOperations = reactiveMongoOperations;
    }

    @Override
    public Mono<Reservation> getReservation(String id) {
        return this.reactiveMongoOperations.findById(id, Reservation.class);
    }

    @Override
    public Mono<Reservation> createReservation(Mono<Reservation> reservationMono) {
        return this.reactiveMongoOperations.save(reservationMono);
    }

    @Override
    public Mono<Reservation> updateReservation(String id, Mono<Reservation> reservationMono) {
        // return this.reactiveMongoOperations.save(reservationMono);
        return reservationMono.flatMap( reservation -> reactiveMongoOperations.findAndModify(
                Query.query(Criteria.where("id").is(id)),
                Update.update("price", reservation.getPrice()), Reservation.class
        ).flatMap( result -> {
            result.setPrice(reservation.getPrice());
            return Mono.just(result);
        }));
    }

    @Override
    public Mono<Boolean> deleteReservation(String id) {
        return reactiveMongoOperations.remove(
                Query.query(Criteria.where("id").is(id)), Reservation.class
        ).flatMap( deleteResult -> Mono.just(deleteResult.wasAcknowledged()));
    }

    @Override
    public Flux<Reservation> listAllReservations() {
        return reactiveMongoOperations.findAll(Reservation.class);
    }
}
