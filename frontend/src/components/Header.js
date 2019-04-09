import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import React from "react";
import './Header.css';

function Header() {
    return (
        <Row>
            <Col className="Header text-center"><span>Elite12 // </span><span>Radio</span></Col>
        </Row>
    );
}

export default Header;