import React, {Component} from 'react';
import Container from 'react-bootstrap/Container';
import {Link} from "react-router-dom";
import {TransitionGroup} from "react-transition-group";
import CSSTransition from "react-transition-group/CSSTransition";
import Moment from 'react-moment';

import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';
import Alert from "react-bootstrap/Alert";

import AuthenticationContext from '../components/AuthenticationContext';
import GravatarIMG from "../components/GravatarIMG";
import AddSong from "../components/AddSong";

import './Home.css';
import { FaTrashAlt } from 'react-icons/fa';

class Home extends Component {

    static contextType = AuthenticationContext;

    constructor(props) {
        super(props);
        this.state = {
            status: 'Loading...',
            songtitle: 'Loading...',
            songlink: null,
            duration: 0,
            playlist: [],
            alerts: []
        };

        this.addAlert=this.addAlert.bind(this);
        this.removeAlert=this.removeAlert.bind(this);
        this.sendStart=this.sendStart.bind(this);
        this.sendPause=this.sendPause.bind(this);
        this.sendStop=this.sendStop.bind(this);
        this.sendSkip=this.sendSkip.bind(this);
        this.sendShuffle=this.sendShuffle.bind(this);
        this.sendDelete=this.sendDelete.bind(this);
        this.sendSong=this.sendSong.bind(this);
        this.handlefetchError=this.handlefetchError.bind(this);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        let diff = this.state.alerts.filter(x => !prevState.alerts.includes(x));
        // eslint-disable-next-line
        for (const [key,value] of Object.entries(diff)) {
            if(value.autoclose) {
                setTimeout(() => {this.removeAlert(value.id)},3000);
            }
        }
    }

    componentDidMount() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/status", {
            method: 'GET',
            headers: headers
        })
        .then((res) => {
            if(!res.ok) throw Error(res.statusText);
            return res;
        })
        .then(res => res.json())
        .then(response => {
            this.setState(response);
        })
        .catch(reason => {
            this.handlefetchError(reason);
        });
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

    sendStart() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/control/start", {
            method: 'POST',
            headers: headers
        }).then((res) => {
            if(!res.ok) throw Error(res.statusText);
        })
        .catch(reason => {
            this.handlefetchError(reason);
        });
    }

    sendPause() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/control/pause", {
            method: 'POST',
            headers: headers
        }).then((res) => {
            if(!res.ok) throw Error(res.statusText);
        })
        .catch(reason => {
            this.handlefetchError(reason);
        });
    }

    sendStop() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/control/stop", {
            method: 'POST',
            headers: headers
        }).then((res) => {
            if(!res.ok) throw Error(res.statusText);
        })
        .catch(reason => {
            this.handlefetchError(reason);
        });
    }

    sendSkip() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/control/skip", {
            method: 'POST',
            headers: headers
        }).then((res) => {
            if(!res.ok) throw Error(res.statusText);
        })
            .catch(reason => {
                this.handlefetchError(reason);
            });
    }

    sendShuffle() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/control/shuffle", {
            method: 'POST',
            headers: headers
        }).then((res) => {
            if(!res.ok) throw Error(res.statusText);
        })
            .catch(reason => {
                this.handlefetchError(reason);
            });
    }

    sendDelete(id, lock) {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/songs/"+id+(lock ? "?lock=true" : ""), {
            method: 'DELETE',
            headers: headers
        }).then((res) => {
            if(!res.ok) throw Error(res.statusText);
        })
        .catch(reason => {
            this.handlefetchError(reason);
        });
    }

    sendSong(url) {
        let headers = new Headers();
        headers.append("Content-Type", "text/plain");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);

        fetch("/api/v2/songs", {
            method: 'POST',
            body: url,
            headers: headers
        }).then((res) => {
            if(!res.ok) throw Error(res.statusText);
            return res;
        })
        .then((res) => res.json())
        .then((res) => {
            let type = res.success ? 'success' : 'danger';
            if(res.warn && res.success) type = 'warning';
            this.addAlert({
                id: Math.random().toString(36),
                type: type,
                text: res.message,
                autoclose: true
            });
        })
        .catch(reason => {
            this.handlefetchError(reason);
        });
    }

    handlefetchError(e) {
        this.addAlert({
            id: Math.random().toString(36),
            type: 'danger',
            head: 'Es ist ein Fehler aufgetreten',
            text: e.message,
            autoclose: false
        });
    }

    render() {
        return (
            <Container fluid>
                <Row className="justify-content-center">
                    <Col xl={{span: 5}} md={{span: 8}} xs={{span: 10}} className="alerts">
                        <Alerts onClose={this.removeAlert}>{this.state.alerts}</Alerts>
                    </Col>
                </Row>
                <Row>
                    <Col className="Header text-center"><span>Elite12 // </span><span>Radio</span></Col>
                </Row>
                <Status state={this.state.status} title={this.state.songtitle} link={this.state.songlink} duration={this.state.duration} />
                {this.context.user && this.context.user.admin && <ControlElements onStart={this.sendStart} onPause={this.sendPause} onStop={this.sendStop} onSkip={this.sendSkip}/>}
                <Playlist AuthState={this.context} onDelete={this.sendDelete} songs={this.state.playlist} />
                <BottomControl onShuffle={this.sendShuffle} />
                <AddSong handlefetchError={this.handlefetchError} sendSong={this.sendSong}/>
            </Container>
        );
    }
}

function Playlist(props) {
    return (
        <Row className="space-top justify-content-center">
            <table className="playlist col-xl-9 col-lg-10 col-md-12">
                <thead>
                    <tr className="header">
                        <th className="d-none d-sm-table-cell songid">Song ID</th>
                        <th className="d-none d-md-table-cell insertat">Eingefügt am</th>
                        <th className="d-none d-sm-table-cell author">Eingefügt von</th>
                        <th className="songtitle">Titel</th>
                        <th className="d-none d-sm-table-cell songlink">Link</th>
                        { props.AuthState.user && props.AuthState.user.admin && <th className="delete"></th>}
                    </tr>
                </thead>
                <tbody>
                    <TransitionGroup component={null}>
                        {props.songs.map((song) => {
                            return (
                                <CSSTransition key={song.id} timeout={300} classNames="song-anim">
                                    <Song AuthState={props.AuthState} onDelete={props.onDelete} key={song.id} {...song} />
                                </CSSTransition>
                            );
                        })}
                    </TransitionGroup>
                </tbody>
            </table>
        </Row>
    );
}

function Song(props) {
    return (
        <tr className="song">
            <td className="d-none d-sm-table-cell">{ props.id }</td>
            <td className="d-none d-md-table-cell"><Moment format="DD.MM.YYYY - HH:mm:ss">{ props.insertedAt }</Moment></td>
            <td className="d-none d-sm-inline-flex"><GravatarIMG>{ props.gravatarId }</GravatarIMG><Link to={`/users/${props.authorLink}`}>{ props.author }</Link></td>
            <td className="nolink songtitle"><a href={ props.link }>{ props.title }</a></td>
            <td className="d-none d-sm-table-cell songlink"><a href={props.link}>{ props.link }</a></td>
            { props.AuthState.user && props.AuthState.user.admin && <td className="d-inline-flex deleteicon" onClick={(e) => {props.onDelete(props.id,e.shiftKey)}}><FaTrashAlt /></td>}
        </tr>
    );
}

function Alerts(props) {
    return (
        <TransitionGroup component={null}>
            {props.children.map((alert) => {
                return (
                    <CSSTransition key={alert.id} timeout={300} classNames="slidedown">
                        <Alert dismissible variant={alert.type} onClose={() => {props.onClose(alert.id)}}>
                            {alert.head && <Alert.Heading>{alert.head}</Alert.Heading>}
                                {alert.text}
                        </Alert>
                    </CSSTransition>
                );
            })}
        </TransitionGroup>
    );
}


function Status(props) {
    return (
        <Row className="justify-content-center">
            <Col className="Status" xl={{span: 5}} md={{span: 8}} xs={{span: 10}}>
                <Row>
                    <Col>{ props.state }</Col>
                    <Col className="text-right" md="auto">Die Aktuelle Playlist umfasst { props.duration } Minuten Musik!</Col>
                </Row>
                <Row>
                    <Col><a href={ props.link }>{ props.title }</a></Col>
                </Row>
            </Col>
        </Row>
    );
}

function ControlElements(props) {
    return (
        <Row className="justify-content-center">
            <Col className="Control" xl={{span: 5}} md={{span: 8}} xs={{span: 10}}>
                <Row noGutters={false}>
                    <Col><Button onClick={props.onStart}>Start</Button></Col>
                    <Col><Button onClick={props.onPause}>Pause</Button></Col>
                    <Col><Button onClick={props.onStop}>Stop</Button></Col>
                    <Col><Button onClick={props.onSkip}>Skip</Button></Col>
                </Row>
            </Col>
        </Row>
    );
}

function BottomControl(props) {
    return (
        <Row className="justify-content-center">
            <Col className="BottomControl" xl={{span: 9}} lg={{span: 10}} md={{span: 12}}>
                <Row noGutters={false}>
                    <Col xs={{span: 6}}><Link to="/archiv">Zum Archiv</Link></Col>
                    <Col xs={{span: 6}}><Button onClick={props.onShuffle}>Shuffle</Button></Col>
                </Row>
            </Col>
        </Row>
    );
}

export default Home;