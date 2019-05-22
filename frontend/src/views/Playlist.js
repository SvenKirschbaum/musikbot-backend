import React, {Component} from "react";
import AuthenticationContext from "../components/AuthenticationContext";
import Container from "react-bootstrap/Container";

import './Playlist.css';
import Alerts from "../components/Alerts";
import Header from "../components/Header";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import Button from "react-bootstrap/Button";
import TransitionGroup from "react-transition-group/TransitionGroup";
import CSSTransition from "react-transition-group/CSSTransition";

class Playlist extends Component {
    static contextType = AuthenticationContext;

    constructor(props) {
        super(props);

        this.state = {
            alerts: [],
            url: "",
            songs: [],
            checkboxes: [],
            name: "",
            link: ""
        };

        this.onCheckbox = this.onCheckbox.bind(this);
        this.removeAlert = this.removeAlert.bind(this);
        this.addAlert = this.addAlert.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
        this.handleLoad = this.handleLoad.bind(this);
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

    handleInputChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.name;

        this.setState({
            [name]: value
        });
    }

    handleLoad(e) {
        e.preventDefault();

        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/playlist?url=" + encodeURIComponent(this.state.url), {
            method: 'GET',
            headers: headers
        })
        .then((res) => {
            if(res.status === 404) {
                this.addAlert({
                    id: Math.random().toString(36),
                    type: 'danger',
                    text: "Playlist nicht gefunden",
                    autoclose: true
                });
                return;
            }
            if(!res.ok) throw Error(res.statusText);

            res.json().then(value => this.setState({...value, checkboxes: new Array(value.songs.length).fill(true)}));
        })
        .catch(reason => {
            console.log(reason);
            this.addAlert({
                id: Math.random().toString(36),
                type: 'danger',
                head: 'Es ist ein Fehler aufgetreten',
                text: reason.message,
                autoclose: false
            });
        });
    }

    onSubmit() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/playlist", {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(this.state.songs.filter((value,index) => this.state.checkboxes[index]))
        })
        .then((res) => {
            if(!res.ok) throw Error(res.statusText);
            return res;
        })
        .then((res) => res.json())
        .then((res) => {
            let results = [];
            res.forEach((e,key) => {
                let type = e.success ? 'success' : 'danger';
                if (e.warn && e.success) type = 'warning';
                results.push(
                    <li key={key} className={type}>
                        {e.message}
                    </li>
                );
            });

            let note = (<ul>{results}</ul>);

            this.addAlert({
                id: Math.random().toString(36),
                type: 'info',
                head: 'Playlist importiert',
                text: note,
                autoclose: false
            });
        })
        .catch(reason => {
            console.log(reason);
            this.addAlert({
                id: Math.random().toString(36),
                type: 'danger',
                head: 'Es ist ein Fehler aufgetreten',
                text: reason.message,
                autoclose: false
            });
        });
    }

    onCheckbox(id,e) {
        var checkboxes = [...this.state.checkboxes];
        checkboxes[id] = e.target.checked;
        this.setState({checkboxes: checkboxes});
    }

    render() {
        return (
            <Container fluid>
                <Alerts onClose={this.removeAlert}>{this.state.alerts}</Alerts>
                <Header />
                <Row className="justify-content-center space-bottom">
                    <Col xl={{span: 9}} lg={{span: 10}} md={{span: 11}} xs={{span: 11}}>
                        <table className="importlist">
                            <thead>
                                <tr className="playlisturl">
                                    <th colSpan={3}>
                                        <form onSubmit={this.handleLoad}>
                                            <Row className="align-items-center">
                                                <Col xl={{span:6}} xs={{span:12}} className="playlisttitle"><a href={this.state.link}>{this.state.name}</a></Col>
                                                <Col xl={{span:5}} xs={{span:8}}><input type="text" onChange={this.handleInputChange} name="url" value={this.state.url} /></Col>
                                                <Col xl={{span:1}} xs={{span:4}}><Button type="submit">Laden</Button></Col>
                                            </Row>
                                        </form>
                                    </th>
                                </tr>
                                {this.state.songs.length>0 && (
                                    <tr>
                                        <th></th>
                                        <th>Titel</th>
                                        <th>Link</th>
                                    </tr>
                                )}
                            </thead>
                            <tbody>
                                <TransitionGroup component={null} exit={false}>
                                {this.state.songs.map((song,key) => (
                                    <CSSTransition key={song.id} timeout={300} classNames="song-anim">
                                        <Song key={song.link + key} checked={this.state.checkboxes[key]} onChange={this.onCheckbox.bind(this,key)} title={song.name} link={song.link} />
                                    </CSSTransition>
                                ))}
                                </TransitionGroup>
                            </tbody>
                            <tfoot>
                            {this.state.songs.length>0 && (
                                <tr>
                                    <th></th>
                                    <th></th>
                                    <th>
                                        <Button disabled={this.state.songs.length===0} onClick={this.onSubmit}>Einfügen</Button>
                                    </th>
                                </tr>
                            )}
                            </tfoot>
                        </table>
                    </Col>
                </Row>
            </Container>
        );
    }
}

function Song(props) {
    return (
        <tr>
            <td><input type="checkbox" checked={props.checked} onChange={props.onChange} /></td>
            <td>{props.title}</td>
            <td><a href={props.link}>{props.link}</a></td>
        </tr>
    );
}

export default Playlist;