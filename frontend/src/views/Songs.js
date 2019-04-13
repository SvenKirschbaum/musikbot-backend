import React, {Component} from 'react';
import Container from 'react-bootstrap/Container';

import AuthenticationContext from '../components/AuthenticationContext';
import Header from '../components/Header';
import Alerts from '../components/Alerts';

import './Songs.css';
import {FaTrashAlt} from "react-icons/fa";
import AddSong from "../components/AddSong";

class Songs extends Component {

    static contextType = AuthenticationContext;

    constructor(props) {
        super(props);
        this.state = {
            alerts: [],
            songs: []
        };

        this.addAlert=this.addAlert.bind(this);
        this.removeAlert=this.removeAlert.bind(this);
        this.load=this.load.bind(this);
        this.sendDelete=this.sendDelete.bind(this);
        this.sendSong=this.sendSong.bind(this);
    }

    componentDidMount() {
        this.load();
    }

    load() {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/lockedsongs", {
            method: 'GET',
            headers: headers
        })
        .then((res) => {
            if(!res.ok) throw Error(res.statusText);
            return res;
        })
        .then(res => res.json())
        .then(res => {
            this.setState({
                songs: res
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

    sendDelete(id) {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if (this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/lockedsongs/" + id, {
            method: 'DELETE',
            headers: headers
        }).then((res) => {
            if (!res.ok) throw Error(res.statusText);
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

    sendSong(url) {
        let headers = new Headers();
        headers.append("Content-Type", "text/plain");
        if (this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/lockedsongs", {
            method: 'POST',
            body: url,
            headers: headers
        }).then((res) => {
            if (!res.ok) throw Error(res.statusText);
            return res;
        })
        .then((res) => res.json())
        .then((res) => {
            let type = res.success ? 'success' : 'danger';
            if (res.warn && res.success) type = 'warning';
            this.addAlert({
                id: Math.random().toString(36),
                type: type,
                text: res.message,
                autoclose: true
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
        })
        .finally(() => {
            this.load();
        });
    }

    render() {
        return (
            <Container fluid className="lockedsongs">
                <Alerts onClose={this.removeAlert}>{this.state.alerts}</Alerts>
                <Header />
                <SongList data={this.state.songs} onDelete={this.sendDelete} />
                <AddSong sendSong={this.sendSong} buttontext="Sperren" />
            </Container>
        );
    }
}

function SongList(props) {
    return (
        <table className="table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Titel</th>
                <th>URL</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
                {props.data.map((entry) => (
                    <tr key={entry.id}>
                        <td>{entry.id}</td>
                        <td>{entry.title}</td>
                        <td><a href={entry.url}>{entry.url}</a></td>
                        <td className="deleteicon" onClick={(e) => props.onDelete(entry.id)}><FaTrashAlt/></td>
                    </tr>
                ))}
            </tbody>
        </table>
    );
}

export default Songs;