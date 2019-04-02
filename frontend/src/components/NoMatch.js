import React, { Component } from 'react';
import Card from 'react-bootstrap/Card';
import Container from 'react-bootstrap/Container';
import {LinkContainer} from 'react-router-bootstrap';

import BaseLayout from './BaseLayout.js';

class NoMatch extends Component {
  render() {
    return (
        <BaseLayout>
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
        </BaseLayout>
    );
  }
}

export default NoMatch;