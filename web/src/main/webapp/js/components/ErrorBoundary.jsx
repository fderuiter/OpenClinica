import React from 'react';
import { store } from '../store.js';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      errorId: null,
    };
    this.handleRetry = this.handleRetry.bind(this);
  }

  static getDerivedStateFromError(error) {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    const errorId = `err_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;

    // Output detailed debug messages using the existing logging configuration
    if (typeof window !== 'undefined' && typeof window.debug === 'function') {
      window.debug(
        `[ErrorBoundary] Caught error: ${error}\n${errorInfo.componentStack}`,
        window.util_logDebug || 2
      );
    } else {
      console.error('[ErrorBoundary] Caught error:', error, errorInfo);
    }

    const currentState = store.getState();
    const currentErrors = currentState.errors || {};

    store.setState({
      errors: {
        ...currentErrors,
        [errorId]: {
          error: error,
          errorInfo: errorInfo,
          timestamp: Date.now(),
        },
      },
    });

    this.setState({ errorId });
  }

  handleRetry() {
    const { errorId } = this.state;
    if (errorId) {
      const currentState = store.getState();
      const currentErrors = { ...currentState.errors };
      delete currentErrors[errorId];
      store.setState({ errors: currentErrors });
    }
    this.setState({ hasError: false, errorId: null });
  }

  render() {
    if (this.state.hasError) {
      return (
        <div
          className="error-boundary-recovery-card"
          style={{
            padding: '20px',
            border: '1px solid #ff4d4f',
            borderRadius: '4px',
            backgroundColor: '#fff2f0',
            margin: '10px',
          }}
        >
          <h3>Something went wrong.</h3>
          <p>We&apos;re sorry, but a part of the interface has crashed.</p>
          <button
            onClick={this.handleRetry}
            style={{
              padding: '8px 16px',
              backgroundColor: '#1890ff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
            }}
          >
            Retry
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
