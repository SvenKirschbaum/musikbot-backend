import React, {Component} from 'react';
import Container from 'react-bootstrap/Container';
import {Link} from "react-router-dom";
import {TransitionGroup} from "react-transition-group";
import CSSTransition from "react-transition-group/CSSTransition";
import Moment from 'react-moment';
import { withRouter } from "react-router";

import Row from 'react-bootstrap/Row';
import Pagination from "react-js-pagination";

import AuthenticationContext from '../components/AuthenticationContext';
import Header from '../components/Header';
import Alerts from '../components/Alerts';
import GravatarIMG from "../components/GravatarIMG";

import './Archiv.css';

class Archiv extends Component {

    static contextType = AuthenticationContext;

    constructor(props) {
        super(props);
        this.state = {
            list: [],
            page: 1,
            pages: 1,
            alerts: []
        };

        this.addAlert=this.addAlert.bind(this);
        this.removeAlert=this.removeAlert.bind(this);
        this.change = this.change.bind(this);
    }

    componentDidMount() {
        this.load((this.props.match.params.page === undefined ? 1 : this.props.match.params.page));
    }

    load(page) {
        let headers = new Headers();
        headers.append("Content-Type", "application/json");
        if(this.context.token) headers.append("Authorization", "Bearer " + this.context.token);
        fetch("/api/v2/archiv/"+page, {
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
                    page: res.page,
                    pages: res.pages,
                    list: res.list
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

    change(page) {
        this.props.history.push('/archiv/'+(page));
        this.load(page);
    }

    render() {
        return (
            <Container fluid>
                <Alerts onClose={this.removeAlert}>{this.state.alerts}</Alerts>
                <Header />
                <Archivlist songs={this.state.list} />
                <Row className="justify-content-center archivepager">
                    <Pagination
                        activePage={this.state.page}
                        itemsCountPerPage={25}
                        totalItemsCount={25*this.state.pages}
                        pageRangeDisplayed={5}
                        onChange={this.change}
                        itemClass="page-item"
                        linkClass="page-link"
                        firstPageText="First"
                        lastPageText="Last"
                        hideNavigation={true}
                    />
                </Row>
            </Container>
        );
    }
}

function Archivlist(props) {
    return (
        <Row className="justify-content-center">
            <table className="playlist col-xl-9 col-lg-10 col-md-12 lr-space archiv">
                <thead>
                    <tr className="header">
                        <th className="d-none d-sm-table-cell songid">Song ID</th>
                        <th className="insertat">Gespielt um</th>
                        <th className="d-none d-sm-table-cell author">Eingefügt von</th>
                        <th className="songtitle">Titel</th>
                        <th className="d-none d-md-table-cell songlink">Link</th>
                    </tr>
                </thead>
                    <tbody>
                        <TransitionGroup component={null} exit={false}>
                            {props.songs.map((song) => (
                                <CSSTransition key={song.id} timeout={300} classNames="song-anim">
                                    <Song key={song.id} {...song} />
                                </CSSTransition>
                            ))}
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
            <td className=""><span className="d-none d-sm-inline"><Moment format="DD.MM.YYYY">{ props.playedAt }</Moment> - </span><Moment format="HH:mm:ss">{ props.playedAt }</Moment></td>
            <td className="d-none d-sm-inline-flex author"><GravatarIMG>{ props.gravatarId }</GravatarIMG><Link to={`/users/${props.authorLink}`}>{ props.author }</Link></td>
            <td className="nolink songtitle"><a href={ props.link }>{ props.title }</a></td>
            <td className="d-none d-md-table-cell songlink"><a href={props.link}>{ props.link }</a></td>
        </tr>
    );
}

export default withRouter(Archiv);