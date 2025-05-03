import React, { useState } from 'react';
import { Container, Row, Col, Button } from 'react-bootstrap';
import RentalList from '../components/RentalList';
import RentalForm from '../components/RentalForm';

const RentalsPage = () => {
    const [showRentalModal, setShowRentalModal] = useState(false);
    const [refreshRentals, setRefreshRentals] = useState(0);

    const handleShowRentalModal = () => setShowRentalModal(true);
    const handleCloseRentalModal = () => setShowRentalModal(false);

    const handleRentalAction = () => {
        setRefreshRentals(prev => prev + 1);
    }

    return (
        <Container className="mt-4">
            <Row className="mb-3 align-items-center">
                <Col>
                    <h2>Управление Арендой Велосипедов</h2>
                </Col>
                <Col className="text-end">
                    <Button variant="primary" onClick={handleShowRentalModal}>
                        Создать Аренду
                    </Button>
                </Col>
            </Row>

            <RentalList key={refreshRentals} onRentalAction={handleRentalAction} />

            <RentalForm
                show={showRentalModal}
                handleClose={handleCloseRentalModal}
                onRentalCreated={handleRentalAction}
            />
        </Container>
    );
};

export default RentalsPage;