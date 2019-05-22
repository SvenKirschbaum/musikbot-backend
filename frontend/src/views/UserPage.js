import React, {Component} from 'react';
import Container from 'react-bootstrap/Container';

import AuthenticationContext from '../components/AuthenticationContext';
import Header from '../components/Header';
import Alerts from '../components/Alerts';

import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Card from 'react-bootstrap/Card'
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';

import './UserPage.css';
import GravatarIMG from "../components/GravatarIMG";
import {TransitionGroup} from "react-transition-group";
import CSSTransition from "react-transition-group/CSSTransition";

class UserPage extends Component {

    static contextType = AuthenticationContext;

    constructor(props) {
        super(props);
        this.state = {
            alerts: [],
            user: {
                recent: [],
                mostwished: [],
                mostskipped: [],
                guest: true,
            },
            modaltype: "",
            modalvalue: "",
            showmodal: false,
        };

        this.addAlert=this.addAlert.bind(this);
        this.removeAlert=this.removeAlert.bind(this);
        this.load = this.load.bind(this);
        this.showedit = this.showedit.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleSave = this.handleSave.bind(this);
        this.handleChange = this.handleChange.bind(this);
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
        if (this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/user/" + this.props.match.params.name, {
            method: 'GET',
            headers: headers
        })
            .then((res) => {
                if (!res.ok) throw Error(res.statusText);
                return res;
            })
            .then((res) => res.json())
            .then(value => this.setState({user: value}))
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

    showedit(type,e) {
        if (type === "username") {
            this.setState({modalvalue: this.state.user.name});
        } else if (type === "email") {
            this.setState({modalvalue: this.state.user.email});
        } else if (type === "password") {
            this.setState({modalvalue: '******'});
        } else if (type === "admin") {
            this.setState({modalvalue: this.state.user.admin ? "Ja" : "Nein"});
        }
        this.setState({showmodal: true, modaltype: type});
    }

    handleClose() {
        this.setState({showmodal: false});
    }

    handleSave() {
        this.handleClose();
        let headers = new Headers();
        if (this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/user/"+this.state.user.id+"/"+this.state.modaltype, {
            method: 'POST',
            headers: headers,
            body: this.state.modalvalue
        })
        .then((res) => {
            if (!res.ok) throw Error(res.statusText);
            return res;
        })
        .then((res) => {
            this.addAlert({
                id: Math.random().toString(36),
                type: 'success',
                head: 'Änderung durchgeführt',
                autoclose: true
            });
            if(this.state.modaltype === "username") {
                this.props.history.push('/users/'+this.state.modalvalue);
            }
            if(this.state.user.id === this.context.user.id) {
                this.context.reload();
            }
            this.load();
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

    handleChange(event) {
        this.setState({modalvalue: event.target.value});
    }

    render() {
        return (
            <Container fluid className="h-100 d-flex flex-column">
                <Alerts onClose={this.removeAlert}>{this.state.alerts}</Alerts>
                <Header />
                <Modal show={this.state.showmodal} onHide={this.handleClose}>
                    <Modal.Header closeButton>
                        <Modal.Title>Edit: {this.state.modaltype}</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <input className="modalinput" type={this.state.modaltype === "password" ? "password" : "text"} value={this.state.modalvalue} onChange={this.handleChange}></input>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={this.handleClose}>
                            Abbrechen
                        </Button>
                        <Button variant="primary" onClick={this.handleSave}>
                            Speichern
                        </Button>
                    </Modal.Footer>
                </Modal>
                <Container fluid className="userpage">
                    <Row>
                        <Col xl={3}>
                            <Card className="usercard profilepic">
                                <Card.Body>
                                    <GravatarIMG animate={true} size="250">{this.state.user.gravatarId}</GravatarIMG>
                                </Card.Body>
                            </Card>
                        </Col>
                        <Col>
                            <Card className="usercard">
                                <Card.Body>
                                    <Card.Title>Allgemein</Card.Title>
                                    <table>
                                        <tbody>
                                            <TransitionGroup component={null} exit={false}>
                                                {(!this.state.user.guest) &&
                                                    <CSSTransition key="id" timeout={300} classNames="fade">
                                                        <tr>
                                                            <td>ID:</td>
                                                            <td>{this.state.user.id}</td>
                                                        </tr>
                                                    </CSSTransition>
                                                }
                                                <CSSTransition key="username" timeout={300} classNames="fade">
                                                    <tr>
                                                        <td>Username:</td>
                                                        <td>
                                                            {this.state.user.name}
                                                            {this.context.user.admin &&
                                                                <span href="#" className="changelink" onClick={(e) => this.showedit("username",e)}>(Ändern)</span>
                                                            }
                                                        </td>
                                                    </tr>
                                                </CSSTransition>
                                                {(!this.state.user.guest) && (this.context.user.admin || (this.state.user.id === this.context.user.id)) &&
                                                    <CSSTransition key="email" timeout={300} classNames="fade">
                                                        <tr>
                                                            <td>Email:</td>
                                                            <td>
                                                                {this.state.user.email}
                                                                <span href="#" className="changelink" onClick={(e) => this.showedit("email",e)}>(Ändern)</span>
                                                            </td>
                                                        </tr>
                                                    </CSSTransition>
                                                }
                                                {(!this.state.user.guest) && (this.context.user.admin || (this.state.user.id === this.context.user.id)) &&
                                                    <CSSTransition key="password" timeout={300} classNames="fade">
                                                        <tr>
                                                            <td>Passwort:</td>
                                                            <td>
                                                                ******
                                                                <span href="#" className="changelink" onClick={(e) => this.showedit("password",e)}>(Ändern)</span>
                                                            </td>
                                                        </tr>
                                                    </CSSTransition>
                                                }
                                                {(!this.state.user.guest) &&
                                                    <CSSTransition key="admin" timeout={300} classNames="fade">
                                                        <tr>
                                                            <td>Admin:</td>
                                                            <td>
                                                                {this.state.user.admin ? "Ja" : "Nein"}
                                                                {this.context.user.admin &&
                                                                    <span href="#" className="changelink" onClick={(e) => this.showedit("admin",e)}>(Ändern)</span>
                                                                }
                                                            </td>
                                                        </tr>
                                                    </CSSTransition>
                                                }
                                                <CSSTransition key="wuensche" timeout={300} classNames="fade">
                                                    <tr>
                                                        <td>Wünsche:</td>
                                                        <td>{this.state.user.wuensche}</td>
                                                    </tr>
                                                </CSSTransition>
                                                <CSSTransition key="skipped" timeout={300} classNames="fade">
                                                    <tr>
                                                        <td>Davon übersprungen:</td>
                                                        <td>{this.state.user.skipped}</td>
                                                    </tr>
                                                </CSSTransition>
                                            </TransitionGroup>
                                        </tbody>
                                    </table>
                                </Card.Body>
                            </Card>
                            <Card className="usercard">
                                <Card.Body>
                                    <Card.Title>Zuletzt gewünscht:</Card.Title>
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Nr.</th>
                                                <th>Titel</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <TransitionGroup component={null} exit={false}>
                                                {this.state.user.recent.map(
                                                    (entry,key) => (
                                                        <CSSTransition key={key} timeout={300} classNames="fade">
                                                            <tr key={key}>
                                                                <td>{entry.id}</td>
                                                                <td><a href={entry.link}>{entry.title}</a></td>
                                                            </tr>
                                                        </CSSTransition>
                                                    )
                                                )}
                                            </TransitionGroup>
                                        </tbody>
                                    </table>
                                </Card.Body>
                            </Card>
                        </Col>
                        <Col>
                            <Card className="usercard">
                                <Card.Body>
                                    <Card.Title>Am meisten gewünscht:</Card.Title>
                                    <table>
                                        <thead>
                                        <tr>
                                            <th>Nr.</th>
                                            <th>Titel</th>
                                            <th>Anzahl</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                            <TransitionGroup component={null} exit={false}>
                                                {this.state.user.mostwished.map(
                                                    (entry,key) => (
                                                        <CSSTransition key={key} timeout={300} classNames="fade">
                                                            <tr key={key}>
                                                                <td>{key+1}.</td>
                                                                <td><a href={entry.link}>{entry.title}</a></td>
                                                                <td>{entry.count}</td>
                                                            </tr>
                                                        </CSSTransition>
                                                    )
                                                )}
                                            </TransitionGroup>
                                        </tbody>
                                    </table>
                                </Card.Body>
                            </Card>
                            <Card className="usercard">
                                <Card.Body>
                                    <Card.Title>Am meisten geskippt:</Card.Title>
                                    <table>
                                        <thead>
                                        <tr>
                                            <th>Nr.</th>
                                            <th>Titel</th>
                                            <th>Anzahl</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                            <TransitionGroup component={null} exit={false}>
                                                {this.state.user.mostskipped.map(
                                                    (entry,key) => (
                                                        <CSSTransition key={key} timeout={300} classNames="fade">
                                                            <tr key={key}>
                                                                <td>{key+1}.</td>
                                                                <td><a href={entry.link}>{entry.title}</a></td>
                                                                <td>{entry.count}</td>
                                                            </tr>
                                                        </CSSTransition>
                                                    )
                                                )}
                                            </TransitionGroup>
                                        </tbody>
                                    </table>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>
                </Container>
            </Container>
        );
    }
}

export default UserPage;