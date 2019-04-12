import React, {Component} from 'react';
import Container from 'react-bootstrap/Container';

import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';

import AuthenticationContext from '../components/AuthenticationContext';
import Header from '../components/Header';
import Alerts from '../components/Alerts';

import './Gapcloser.css';

class Gapcloser extends Component {

    static contextType = AuthenticationContext;

    constructor(props) {
        super(props);
        this.state = {
            alerts: [],
            playlist: "",
            mode : ''
        };

        this.addAlert=this.addAlert.bind(this);
        this.removeAlert=this.removeAlert.bind(this);
        this.load = this.load.bind(this);
        this.save = this.save.bind(this);
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
        fetch("/api/v2/gapcloser", {
            method: 'GET',
            headers: headers
        })
        .then((res) => {
            if(!res.ok) throw Error(res.statusText);
            return res;
        })
        .then((res) => res.json())
        .then((res) => {
            this.setState(res);
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

    save(e) {
        e.preventDefault();
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/gapcloser", {
            method: 'POST',
            body: JSON.stringify({mode: this.state.mode, playlist: this.state.playlist}),
            headers: headers
        })
        .then((res) => {
            if(!res.ok) throw Error(res.statusText);
            return res;
        })
        .then((res) => res.json())
        .then((res) => {
            this.setState(res);
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
                    <Col className="gapcloser">
                        Gapcloser - Einstellungen
                        <form>
                            <div>
                                <label>Modus:</label>
                                <select value={this.state.mode} onChange={(e) => this.setState({mode: e.target.value})}>
                                    <option value="OFF">Aus</option>
                                    <option value="RANDOM">Zufällig</option>
                                    <option value="RANDOM100">Zufällig - Top 100</option>
                                    <option value="PLAYLIST">Playlist</option>
                                </select>
                            </div>
                            {this.state.mode === "PLAYLIST" && <div><label>Playlist:</label><input type="text" value={this.state.playlist} onChange={(e) => this.setState({playlist: e.target.value})} /></div>}
                            <button onClick={this.save}>Speichern</button>
                        </form>
                    </Col>
                </Row>
            </Container>
        );
    }
}

export default Gapcloser;