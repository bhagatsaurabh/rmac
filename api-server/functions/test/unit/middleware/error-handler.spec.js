import { jest } from '@jest/globals';
import { APIError, errorHandler, errors } from '../../../middleware/error-handler.js';

let logger = { error: jest.fn(), warn: jest.fn(), log: jest.fn() };

describe('Error handling middleware', () => {
    it("should send 500 with correct message", async () => {
        const req = { context: { logger } };
        const mockFn = jest.fn();
        const res = { status: () => ({ json: mockFn }) };

        errorHandler({}, req, res);

        expect(mockFn).toBeCalledWith({ status: 500, message: 'Something went wrong', code: errors.SERVER_ERROR });
    });
    it("should send 400 with correct message", async () => {
        const req = { context: { logger } };
        const mockFn = jest.fn();
        const res = { status: () => ({ json: mockFn }) };

        const err = new APIError(errors.CLIENT_HOST_NAME_NOT_PROVIDED);

        errorHandler(err, req, res);

        expect(mockFn).toBeCalledWith({ status: 400, message: 'ClientName and HostName must be provided', code: errors.CLIENT_HOST_NAME_NOT_PROVIDED });
    });
    it("should send 404 with correct message", async () => {
        const req = { context: { logger } };
        const mockFn = jest.fn();
        const res = { status: () => ({ json: mockFn }) };

        const err = new APIError(errors.CLIENT_ID_NOT_FOUND);

        errorHandler(err, req, res);

        expect(mockFn).toBeCalledWith({ status: 404, message: 'ClientId not found', code: errors.CLIENT_ID_NOT_FOUND });
    });
});
