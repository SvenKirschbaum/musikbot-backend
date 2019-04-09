import React, { Component } from 'react';
import Moment from "react-moment";

class Clock extends Component {
  constructor(props) {
    super(props);
    this.state = {date: new Date()};
  }

  componentDidMount() {
    this.timerID = setInterval(
      () => {
        this.setState({
          date: new Date()
        });
      },
      1000
    );
  }

  componentWillUnmount() {
    clearInterval(this.timerID);
  }

  render() {
    return (
        <span className={ this.props.className }><Moment format="HH:mm">{ this.state.date }</Moment></span>
    );
  }
}

export default Clock;