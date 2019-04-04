import React, { Component } from 'react';
import Container from 'react-bootstrap/Container';

import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';

import './Home.css';
import Button from 'react-bootstrap/Button';

class Home extends Component {
  render() {
    return (
      <Container>
        <Row>
            <Col className="Header text-center"><span>Elite12 // </span><span>Radio</span></Col>
        </Row>
        <Status />
        <ControlElements />
      </Container>    
    );
  }
}

function Status(props) {
    return (
        <Row className="justify-content-center">
            <Col className="Status" xs={{span:8}}>
                <Row>
                    <Col>Warte auf neue Lieder</Col>
                    <Col className="text-right" md="auto">Die Aktuelle Playlist umfasst 0 Minuten Musik!</Col>
                </Row>
                <Row>
                    <Col>Kein Song!</Col>
                </Row>
            </Col>
        </Row>
    );
}

function ControlElements(props) {
    return (
        <Row className="justify-content-center">
            <Col className="Control" xs={{span:8}}>
                <Row noGutters={false}>
                    <Col><Button>Start</Button></Col>
                    <Col><Button>Pause</Button></Col>
                    <Col><Button>Stop</Button></Col>
                    <Col><Button>Skip</Button></Col>
                </Row>
            </Col>
        </Row>
    );
}

export default Home;