import React, { Component } from 'react';
import Card from 'react-bootstrap/Card';
import Container from 'react-bootstrap/Container';
import {LinkContainer} from 'react-router-bootstrap';

class NoMatch extends Component {
  render() {
    return (
        <Container className="NoMatch vertical-center">
            <Card>
                <Card.Header>Seite nicht gefunden</Card.Header>
                <Card.Body>
                    <Card.Text>
                        Die gewünschte Seite existiert leider nicht.
                    </Card.Text>
                    <LinkContainer to="/">
                        <Card.Link>Zurück zur Startseite</Card.Link>
                    </LinkContainer>
                </Card.Body>
            </Card>
        </Container>
    );
  }
}

export default NoMatch;