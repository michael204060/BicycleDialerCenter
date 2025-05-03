import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import BicyclesPage from './pages/BicyclesPage';
import UsersPage from './pages/UsersPage';
import RentalsPage from './pages/RentalsPage';
import { Nav, Navbar, Container } from 'react-bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';

const App = () => {
    return (
        <Router>
            <Navbar bg="light" expand="lg" className="mb-4">
                <Container>
                    <Navbar.Brand as={Link} to="/">Bicycle App</Navbar.Brand>
                    <Navbar.Toggle aria-controls="basic-navbar-nav" />
                    <Navbar.Collapse id="basic-navbar-nav">
                        <Nav className="me-auto">
                            <Nav.Link as={Link} to="/bicycles">Велосипеды</Nav.Link>
                            <Nav.Link as={Link} to="/users">Пользователи</Nav.Link>
                            <Nav.Link as={Link} to="/rentals">Аренда</Nav.Link>
                        </Nav>
                    </Navbar.Collapse>
                </Container>
            </Navbar>

            <Routes>
                <Route path="/bicycles" element={<BicyclesPage />} />
                <Route path="/users" element={<UsersPage />} />
                <Route path="/rentals" element={<RentalsPage />} />
                <Route path="/" element={
                    <Container className="mt-4">
                        <h2>Добро пожаловать в приложение для управления велосипедами!</h2>
                        <p>Используйте меню навигации выше для просмотра и управления данными о велосипедах и пользователях.</p>
                        <p>Раздел "Аренда" позволяет просматривать и создавать записи об аренде велосипедов пользователями.</p>
                    </Container>
                } />
            </Routes>
        </Router>
    );
};

export default App;