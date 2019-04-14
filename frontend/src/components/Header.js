import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import React, {Component} from "react";
import {withRouter} from "react-router";
import './Header.css';

class Header extends Component {

    constructor(props) {
        super(props);
        this.onClick = this.onClick.bind(this);
    }

    onClick() {
        if(this.props.location.pathname !== '/') {
            this.props.history.push('/');
        }
    }

    render() {
        return (
            <header>
                <Row>
                    <Col onClick={this.onClick} className="Header text-center"><span>Elite12 // </span><span>Radio</span></Col>
                </Row>
            </header>
        );
    }
}

export default withRouter(Header);