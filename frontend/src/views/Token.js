import React, {Component} from 'react';
import Container from 'react-bootstrap/Container';

import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';

import AuthenticationContext from '../components/AuthenticationContext';
import Header from '../components/Header';
import Alerts from '../components/Alerts';

import './Token.css';

class Token extends Component {

    static contextType = AuthenticationContext;

    constructor(props) {
        super(props);
        this.state = {
            token: "",
            alerts: []
        };

        this.addAlert=this.addAlert.bind(this);
        this.removeAlert=this.removeAlert.bind(this);
        this.onReset = this.onReset.bind(this);
    }

    componentDidMount() {
        this.load();
    }

    load(reset = false) {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/user/self/token"+(reset ? "/reset" : ""), {
            method: (reset ? 'POST' : 'GET'),
            headers: headers
        })
        .then((res) => {
            if(!res.ok) throw Error(res.statusText);
            return res;
        })
        .then(res => res.json())
        .then(res => {
            this.setState({
                token: res.token
            });
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

    onReset() {
        this.load(true);
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

    render() {
        return (
            <Container fluid>
                <Alerts onClose={this.removeAlert}>{this.state.alerts}</Alerts>
                <Header />
                <TokenForm token={this.state.token} onReset={this.onReset}/>
            </Container>
        );
    }
}

function TokenForm(props) {
    return (
        <Row className="justify-content-center">
            <Col className="tokenwindow text-center">
                <input type="text" value={props.token} className="text-center w-100" readOnly={true} onClick={(event) => event.target.select()} />
                <button onClick={props.onReset}>Token resetten</button>
            </Col>
        </Row>
    );
}

export default Token;