import supertest from 'supertest';
import { getServer } from '../../../api/index.js';
import { jest } from '@jest/globals';
import { errors } from '../../../middleware/error-handler.js';

let logger = { error: jest.fn(), warn: jest.fn(), log: jest.fn() };

describe('GET /register', () => {
    it("should send error response when clientName is not provided", async () => {
        await supertest(getServer({ logger })).get("/api/register")
            .expect(400)
            .then(res => {
                expect(res.body.status).toEqual(400);
                expect(res.body.code).toEqual(errors.CLIENT_HOST_NAME_NOT_PROVIDED);
            });
    });
    it("should register when clientId is not passed", async () => {
        const mockPush = jest.fn(() => ({ key: 'testid1234' }));
        const mockDB = { ref: () => ({ push: mockPush }) };

        await supertest(getServer({ db: mockDB, logger })).get("/api/register?clientName=Test&hostName=Test")
            .expect(201)
            .then(res => {
                expect(res.text).toEqual('testid1234');
                expect(mockPush).toBeCalledWith({ clientName: 'Test', hostName: 'Test' });
            });
    });
    it("should register when clientId is passed but doesn't exist", async () => {
        const mockPush = jest.fn(() => ({ key: 'testid1234' }));
        const mockDB = { ref: () => ({ push: mockPush, once: () => ({ exists: () => false }) }) };

        await supertest(getServer({ db: mockDB, logger })).get("/api/register?clientName=Test&hostName=Test&id=testid1234")
            .expect(201)
            .then(res => {
                expect(res.text).toEqual('testid1234');
                expect(mockPush).toBeCalledWith({ clientName: 'Test', hostName: 'Test' });
            });
    });
    it("should not register when clientId is passed and already exists", async () => {
        const mockPush = jest.fn(() => ({ key: 'testid1234' }));
        const mockDB = { ref: () => ({ push: mockPush, once: () => ({ exists: () => true }) }) };

        await supertest(getServer({ db: mockDB, logger })).get("/api/register?clientName=Test&hostName=Test&id=testid1234")
            .expect(200)
            .then(res => {
                expect(res.text).toEqual('testid1234');
                expect(mockPush).not.toBeCalled();
            });
    });
});
