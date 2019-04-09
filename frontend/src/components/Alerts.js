import React, {Component} from "react";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import {TransitionGroup} from "react-transition-group";
import CSSTransition from "react-transition-group/CSSTransition";
import Alert from "react-bootstrap/Alert";

import "./Alerts.css";

class Alerts extends Component {

    componentDidUpdate(prevProps, prevState, snapshot) {
        let diff = this.props.children.filter(x => !prevProps.children.includes(x));
        // eslint-disable-next-line
        for (const [key,value] of Object.entries(diff)) {
            if(value.autoclose) {
                setTimeout(() => {this.props.onClose(value.id)},3000);
            }
        }
    }

    render() {
        return (
            <Row className="justify-content-center">
                <Col xl={{span: 5}} md={{span: 8}} xs={{span: 10}} className="alerts">
                    <TransitionGroup component={null}>
                        {this.props.children.map((alert) => {
                            return (
                                <CSSTransition key={alert.id} timeout={300} classNames="slidedown">
                                    <Alert dismissible variant={alert.type} onClose={() => {this.props.onClose(alert.id)}}>
                                        {alert.head && <Alert.Heading>{alert.head}</Alert.Heading>}
                                        {alert.text}
                                    </Alert>
                                </CSSTransition>
                            );
                        })}
                    </TransitionGroup>
                </Col>
            </Row>
        );
    }
}

export default Alerts;