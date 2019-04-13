import React, {Component} from 'react';
import Container from 'react-bootstrap/Container';

import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Card from 'react-bootstrap/Card'

import AuthenticationContext from '../components/AuthenticationContext';
import Header from '../components/Header';
import Alerts from '../components/Alerts';

import './Stats.css';
import CSSTransition from "react-transition-group/CSSTransition";
import {Link} from "react-router-dom";
import {TransitionGroup} from "react-transition-group";

class Stats extends Component {

    static contextType = AuthenticationContext;

    constructor(props) {
        super(props);
        this.state = {
            alerts: [],
            mostplayed: [],
            mostskipped: [],
            topuser: [],
            general: []
        };

        this.addAlert=this.addAlert.bind(this);
        this.removeAlert=this.removeAlert.bind(this);
        this.load = this.load.bind(this);
    }

    componentDidMount() {
        this.load();
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

    load() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/stats", {
            method: 'GET',
            headers: headers
        })
        .then((res) => {
            if(!res.ok) throw Error(res.statusText);
            return res;
        })
        .then((res) => res.json())
        .then(value => this.setState(value))
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
            <Container fluid className="h-100 d-flex flex-column">
                <Alerts onClose={this.removeAlert}>{this.state.alerts}</Alerts>
                <Header />
                <Row className="statsrow">
                    <EntryCard title="Am meisten gewünscht" data={this.state.mostplayed} mapfunction={
                        (entry,key) => (
                            <tr key={key}>
                                <td className="idcolumn">{key+1}.</td>
                                <td><a href={entry.link}>{entry.title}</a></td>
                                <td>{entry.count}</td>
                            </tr>
                        )
                    }
                    header={
                        <tr>
                            <th className="idcolumn">Nr.</th>
                            <th>Titel</th>
                            <th>Anzahl</th>
                        </tr>
                    } />
                    <EntryCard title="Am meisten geskippt" data={this.state.mostskipped} mapfunction={
                        (entry,key) => (
                            <tr key={key}>
                                <td className="idcolumn">{key+1}.</td>
                                <td><a href={entry.link}>{entry.title}</a></td>
                                <td>{entry.count}</td>
                            </tr>
                        )
                    }
                    header={
                        <tr>
                            <th className="idcolumn">Nr.</th>
                            <th>Titel</th>
                            <th>Anzahl</th>
                        </tr>
                    } />

                    <EntryCard title="Top Wünscher" data={this.state.topuser} mapfunction={
                        (entry,key) => (
                            <tr key={key}>
                                <td className="idcolumn">{key+1}.</td>
                                <td><Link to={`/users/${entry.name}`}>{entry.name}</Link></td>
                                <td>{entry.count}</td>
                            </tr>
                        )
                    }
                    header={
                        <tr>
                            <th className="idcolumn">Nr.</th>
                            <th>Name</th>
                            <th>Anzahl</th>
                        </tr>
                    } />

                    <EntryCard title="Allgemeines" data={this.state.general} mapfunction={
                        (entry,key) => (
                            <tr key={key}>
                                <td>{entry.title}:</td>
                                <td>{entry.value}</td>
                            </tr>
                        )
                    }
                    header={
                        <tr>
                            <th>Titel</th>
                            <th>Anzahl</th>
                        </tr>
                    } />
                </Row>
            </Container>
        );
    }
}

function EntryCard(props) {
    return (
        <Col lg={{span: 6}} className="statscard">
            <Card className="h-100">
                <Card.Body>
                    <Card.Title>{props.title}</Card.Title>
                    <table>
                        <thead>
                            {props.header}
                        </thead>
                        <tbody>
                            <TransitionGroup component={null} exit={false}>
                                {props.data.map(
                                    (entry,key) => (
                                        <CSSTransition key={key} timeout={300} classNames="fade">
                                            {props.mapfunction(entry,key)}
                                        </CSSTransition>
                                    )
                                )}
                            </TransitionGroup>
                        </tbody>
                    </table>
                </Card.Body>
            </Card>
        </Col>
    );
}

export default Stats;