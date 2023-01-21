const errorHandler = (err, req, res, _next) => {
    const { logger } = req.context;
    let error;
    if (!(err instanceof APIError)) {
        error = new APIError(errors.SERVER_ERROR);
        error.stack = err.stack;
        err = error;
    }
    logger.error(err);

    const { status, message, code } = err;
    const response = {
        status,
        message,
        code
    };
    if (process.env.NODE_ENV === 'development') {
        res.stack = err.stack;
    }

    res.status(status).json(response);
}

const errorObjs = {
    1: { status: 500, message: 'Something went wrong' },
    2: { status: 400, message: 'ClientName and HostName must be provided' },
    3: { status: 400, message: 'ClientId must be provided' },
    4: { status: 404, message: 'ClientId not found' },
    5: { status: 400, message: 'Bad commands format, must be an array of commands' },
    6: { status: 400, message: 'Currently used version must be provided' },
};

const errors = {
    SERVER_ERROR: 1,
    CLIENT_HOST_NAME_NOT_PROVIDED: 2,
    CLIENT_ID_NOT_PROVIDED: 3,
    CLIENT_ID_NOT_FOUND: 4,
    BAD_COMMANDS_FORMAT: 5,
    VERSION_NOT_PROVIDED: 6,
}

class APIError extends Error {
    constructor(errorId) {
        super();
        this.errorId = errorId;
        this.setError();
    }

    setError() {
        const error = errorObjs[this.errorId];

        this.status = error.status;
        this.message = error.message;
        this.code = this.errorId;
    }
}

export { errorHandler, errors, APIError };
