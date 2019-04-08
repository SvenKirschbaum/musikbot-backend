import React, {Component} from 'react';
import Container from 'react-bootstrap/Container';
import {Link} from "react-router-dom";
import {TransitionGroup} from "react-transition-group";
import CSSTransition from "react-transition-group/CSSTransition";
import FlipMove from "react-flip-move";
import Moment from 'react-moment';
import {DragDropContext,Droppable,Draggable} from "react-beautiful-dnd";

import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';
import Alert from "react-bootstrap/Alert";

import AuthenticationContext from '../components/AuthenticationContext';
import GravatarIMG from "../components/GravatarIMG";
import AddSong from "../components/AddSong";
import DragFixedCell from "../components/DragFixedCell";

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
        this.sendSort=this.sendSort.bind(this);
        this.onDragEnd=this.onDragEnd.bind(this);
        this.update=this.update.bind(this);
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
        this.update();
        this.intervalId = setInterval(this.update, 8000);
    }

    componentWillUnmount(){
        clearInterval(this.intervalId);
    }

    update() {
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
            if(response.songtitle === null) response.songtitle = "Kein Song";
            this.setState(response);
        })
        .catch(reason => {
            clearInterval(this.intervalId);
            this.addAlert({
                id: Math.random().toString(36),
                type: 'danger',
                head: 'Fehler beim Aktualisieren der Playlist',
                text: 'Beim Aktualisieren der Playlist ist ein Fehler aufgetreten. Das automatische Aktualisieren wurde deaktiviert. Bitte lade die Seite neu, um es wieder zu aktivieren. \n\n'+reason,
                autoclose: false
            });
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
            this.update();
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
            if(res.success) this.update();
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

    onDragEnd(result) {
        const {destination, source} = result;
        if(!destination) return;
        if(destination.droppableId === source.droppableId && destination.index === source.index) return;

        const newList = Array.from(this.state.playlist);
        newList.splice(source.index,1);
        newList.splice(destination.index, 0, this.state.playlist[source.index]);

        this.setState({
           playlist: newList
        });

        const prev = (destination.index - 1) >= 0 ? newList[destination.index - 1].id : -1;
        const id = newList[destination.index].id;

        this.sendSort(prev,id);
    }

    sendSort(prev,id) {
        let headers = new Headers();
        headers.append("Content-Type", "text/plain");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/songs/"+id, {
            method: 'PUT',
            headers: headers,
            body: prev
        }).then((res) => {
            if(!res.ok) throw Error(res.statusText);
        })
        .catch(reason => {
            this.handlefetchError(reason);
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
                <Playlist onDragEnd={this.onDragEnd} AuthState={this.context} onDelete={this.sendDelete} songs={this.state.playlist} />
                <BottomControl onShuffle={this.sendShuffle} />
                <AddSong handlefetchError={this.handlefetchError} sendSong={this.sendSong}/>
            </Container>
        );
    }
}

function Playlist(props) {
    return (
        <Row className="space-top justify-content-center">
            <table className="playlist col-xl-9 col-lg-10 col-md-12 lr-space">
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
                <DragDropContext
                    onDragEnd={props.onDragEnd}
                >
                    <Droppable droppableId="droppable">
                        { (provided) => (
                            <tbody ref={provided.innerRef} {...provided.droppableProps} style={{position: 'relative'}}>
                                <FlipMove typeName={null} enterAnimation="fade" leaveAnimation="none" duration={400}>
                                    {props.songs.map((song,index) => {
                                        return (
                                            <Draggable isDragDisabled={!(props.AuthState.user && props.AuthState.user.admin)} key={song.id} draggableId={song.id} index={index}>
                                                {(provided, snapshot) => (
                                                    <Song AuthState={props.AuthState} onDelete={props.onDelete} key={song.id} {...song} provided={provided} isDragging={snapshot.isDragging} />
                                                )}
                                            </Draggable>
                                        );
                                    })}
                                    {provided.placeholder}
                                </FlipMove>
                            </tbody>
                        )}
                    </Droppable>
                </DragDropContext>
            </table>
        </Row>
    );
}

function Song(props) {
    return (
        <tr className={props.isDragging ? "song dragging" : "song"} {...props.provided.draggableProps} ref={props.provided.innerRef}>
            <DragFixedCell isDragOccurring={props.isDragging} className="d-none d-sm-table-cell" addToElem={props.provided.dragHandleProps}>{ props.id }</DragFixedCell>
            <DragFixedCell isDragOccurring={props.isDragging} className="d-none d-md-table-cell"><Moment format="DD.MM.YYYY - HH:mm:ss">{ props.insertedAt }</Moment></DragFixedCell>
            <DragFixedCell isDragOccurring={props.isDragging} className="d-none d-sm-inline-flex"><GravatarIMG>{ props.gravatarId }</GravatarIMG><Link to={`/users/${props.authorLink}`}>{ props.author }</Link></DragFixedCell>
            <DragFixedCell isDragOccurring={props.isDragging} className="nolink songtitle"><a href={ props.link }>{ props.title }</a></DragFixedCell>
            <DragFixedCell isDragOccurring={props.isDragging} className="d-none d-sm-table-cell songlink"><a href={props.link}>{ props.link }</a></DragFixedCell>
            { props.AuthState.user && props.AuthState.user.admin && <DragFixedCell isDragOccurring={props.isDragging} className="d-inline-flex deleteicon" onClick={(e) => {props.onDelete(props.id,e.shiftKey)}}><FaTrashAlt /></DragFixedCell>}
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
                    <Col>{ (props.link) ? <a href={ props.link }>{ props.title }</a> : props.title }</Col>
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
            <Col className="BottomControl lr-space" xl={{span: 9}} lg={{span: 10}} md={{span: 12}}>
                <Row noGutters={false}>
                    <Col xs={{span: 6}}><Link to="/archiv">Zum Archiv</Link></Col>
                    <Col xs={{span: 6}}><Button onClick={props.onShuffle}>Shuffle</Button></Col>
                </Row>
            </Col>
        </Row>
    );
}

export default Home;