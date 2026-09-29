package org.example.reservationservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.reservationservice.kafka.ReservationCreatedProducer;
import org.example.reservationservice.models.constants.ReservationStatus;
import org.example.reservationservice.models.dto.requests.CreateReservationDetailRequest;
import org.example.reservationservice.models.dto.requests.CreateReservationRequest;
import org.example.reservationservice.models.dto.responses.ReservationDetailResponse;
import org.example.reservationservice.models.dto.responses.ReservationResponse;
import org.example.reservationservice.models.dto.responses.RoomResponse;
import org.example.reservationservice.models.entities.Reservation;
import org.example.reservationservice.models.entities.ReservationDetail;
import org.example.reservationservice.models.repositories.ReservationDetailRepository;
import org.example.reservationservice.models.repositories.ReservationRepository;
import org.example.reservationservice.models.services.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

        private final ReservationRepository reservationRepository;
        private final ReservationDetailRepository reservationDetailRepository;
        private final RoomGatewayService roomGatewayService;
        private final ReservationCreatedProducer reservationCreatedProducer;

        @Override
        @Transactional
        public ReservationResponse createReservation(CreateReservationRequest request) {
                List<ReservationDetailResponse> detailResponses = new ArrayList<>();
                double totalAmount = 0.0;

                Reservation reservation = Reservation.builder()
                        .customerName(request.customerName())
                        .customerEmail(request.customerEmail())
                        .status(ReservationStatus.PENDING)
                        .build();

                Reservation savedReservation = reservationRepository.save(reservation);

                try {
                        for (CreateReservationDetailRequest item : request.items()) {
                                RoomResponse room = roomGatewayService.getRoomById(item.roomId());

                                double lineTotal = room.pricePerNight() * item.nights();
                                totalAmount += lineTotal;

                                ReservationDetail detail = ReservationDetail.builder()
                                        .reservation(savedReservation)
                                        .roomId(room.roomId())
                                        .nights(item.nights())
                                        .pricePerNight(room.pricePerNight())
                                        .build();

                                ReservationDetail savedDetail = reservationDetailRepository.save(detail);

                                detailResponses.add(new ReservationDetailResponse(
                                        savedDetail.getId(),
                                        savedDetail.getRoomId(),
                                        room.roomNumber(),
                                        room.roomType(),
                                        savedDetail.getNights(),
                                        savedDetail.getPricePerNight(),
                                        lineTotal
                                ));
                        }

                        savedReservation.setTotalAmount(totalAmount);
                        reservationRepository.save(savedReservation);

                        reservationCreatedProducer.sendReservationCreated(savedReservation.getCustomerEmail());

                        return new ReservationResponse(
                                savedReservation.getId(),
                                savedReservation.getCustomerName(),
                                savedReservation.getCustomerEmail(),
                                savedReservation.getTotalAmount(),
                                savedReservation.getStatus(),
                                detailResponses
                        );
                } catch (RuntimeException ex) {
                        throw new RuntimeException("Failed to create reservation: " + ex.getMessage(), ex);
                }
        }
}