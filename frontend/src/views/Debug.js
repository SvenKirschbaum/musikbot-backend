import React, {Component} from 'react';
import Container from 'react-bootstrap/Container';

import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';

import AuthenticationContext from '../components/AuthenticationContext';
import Header from '../components/Header';
import Alerts from '../components/Alerts';

import './Debug.css';

class Debug extends Component {

    static contextType = AuthenticationContext;

    constructor(props) {
        super(props);
        this.state = {
            alerts: []
        };

        this.addAlert=this.addAlert.bind(this);
        this.removeAlert=this.removeAlert.bind(this);
        this.onServer = this.onServer.bind(this);
        this.onClient = this.onClient.bind(this);
    }

    addAlert(alert) {
        var alerts = [...this.state.alerts];
        alerts.push(alert);
        this.setState({alerts: alerts});
    }

    removeAlert(id) {
        var alerts = [...this.state.alerts]; // make a separate copy of the array
        let index = -1;
        for (const [key, value] of Object.entries(alerts)) {
            if(value.id === id) {
                index = key;
            }
        }
        if (index !== -1) {
            alerts.splice(index, 1);
            this.setState({alerts: alerts});
        }
    }

    onServer() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/debug/server", {
            method: ('POST'),
            headers: headers
        })
        .then((res) => {
            if(!res.ok) throw Error(res.statusText);
            return res;
        })
        .catch(reason => {
            this.addAlert({
                id: Math.random().toString(36),
                type: 'danger',
                head: 'Es ist ein Fehler aufgetreten',
                text: reason.message,
                autoclose: false
            });
        });
    }

    onClient() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/debug/client", {
            method: ('POST'),
            headers: headers
        })
        .then((res) => {
            if(!res.ok) throw Error(res.statusText);
            return res;
        })
        .catch(reason => {
            this.addAlert({
                id: Math.random().toString(36),
                type: 'danger',
                head: 'Es ist ein Fehler aufgetreten',
                text: reason.message,
                autoclose: false
            });
        });
    }

    render() {
        return (
            <Container fluid>
                <Alerts onClose={this.removeAlert}>{this.state.alerts}</Alerts>
                <Header />
                <Row className="justify-content-center">
                    <Col className="text-center debug">
                        <button onClick={this.onServer}>Server neustarten</button>
                        <button onClick={this.onClient}>Client neustarten</button>
                    </Col>
                </Row>
            </Container>
        );
    }
}

export default Debug;