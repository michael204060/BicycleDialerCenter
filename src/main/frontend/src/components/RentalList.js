import React, { useState, useEffect } from 'react';
import { Table, Button, Container, Row, Col, Alert, Spinner } from 'react-bootstrap';
import { getAllRentals, returnBicycle } from '../api';
import moment from 'moment';

const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return 'Текущая аренда';
    try {
        return moment(dateTimeString).format('YYYY-MM-DD HH:mm');
    } catch (e) {
        console.error("Error parsing date:", dateTimeString, e);
        return dateTimeString;
    }
};

const RentalList = ({ onRentalAction }) => {
    const [rentals, setRentals] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [processingRental, setProcessingRental] = useState(null);

    useEffect(() => {
        fetchRentals();
    }, []);

    const fetchRentals = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await getAllRentals();
            setRentals(response.data);
        } catch (err) {
            console.error('Fetch rentals error:', err.response || err.message);
            setError('Не удалось загрузить список аренд. ' + (err.response?.data?.message || err.message));
        } finally {
            setLoading(false);
        }
    };

    const handleReturn = async (userId, bicycleId) => {
        if (!window.confirm('Вы уверены, что хотите отметить возврат этого велосипеда?')) {
            return;
        }
        setProcessingRental({ userId, bicycleId });
        setError(null);

        try {
            const response = await returnBicycle(userId, bicycleId);
            setRentals(rentals.map(rental =>
                rental.userId === userId && rental.bicycleId === bicycleId ? response.data : rental
            ));
            if (onRentalAction) onRentalAction();
        } catch (err) {
            console.error('Return bicycle error:', err.response || err.message);
            const errorMessage = err.response?.data?.message || err.message || 'Неизвестная ошибка при возврате.';
            setError('Ошибка при возврате велосипеда: ' + errorMessage);
        } finally {
            setProcessingRental(null);
        }
    };

    if (loading) {
        return (
            <div className="text-center">
                <Spinner animation="border" size="sm" /> Загрузка аренд...
            </div>
        );
    }

    if (error && rentals.length === 0) {
        return <Alert variant="danger">{error}</Alert>;
    }


    return (
        <>
            {error && rentals.length > 0 && <Alert variant="danger">{error}</Alert>}
            <Table striped bordered hover responsive size="sm">
                <thead>
                <tr>
                    <th>Пользователь</th>
                    <th>Велосипед</th>
                    <th>Время начала</th>
                    <th>Время окончания</th>
                    <th>Действия</th>
                </tr>
                </thead>
                <tbody>
                {rentals.length === 0 && !loading && !error ? (
                    <tr>
                        <td colSpan={5} className="text-center">Нет записей об аренде.</td>
                    </tr>
                ) : (
                    rentals.map((rental) => (
                        <tr key={`${rental.userId}-${rental.bicycleId}`}>
                            <td>{rental.username || 'N/A'}</td>
                            <td>{`${rental.bicycleBrand || 'N/A'} ${rental.bicycleModel || 'N/A'}`}</td>
                            <td>{formatDateTime(rental.rentStartTime)}</td>
                            <td>{formatDateTime(rental.rentEndTime)}</td>
                            <td>
                                <Button
                                    variant="outline-success"
                                    size="sm"
                                    onClick={() => handleReturn(rental.userId, rental.bicycleId)}
                                    disabled={rental.rentEndTime != null || (processingRental && processingRental.userId === rental.userId && processingRental.bicycleId === rental.bicycleId)}
                                >
                                    {processingRental && processingRental.userId === rental.userId && processingRental.bicycleId === rental.bicycleId ? (
                                        <Spinner animation="border" size="sm" />
                                    ) : (
                                        rental.rentEndTime == null ? 'Вернуть' : 'Возвращен'
                                    )}
                                </Button>
                            </td>
                        </tr>
                    ))
                )}
                </tbody>
            </Table>
        </>
    );
};

export default RentalList;