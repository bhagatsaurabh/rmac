import supertest from 'supertest';
import { getServer } from '../../../api/index.js';
import { jest } from '@jest/globals';
import { errors } from '../../../middleware/error-handler.js';

let logger = { error: jest.fn(), warn: jest.fn(), log: jest.fn() };

describe('GET /update', () => {
    it("should send error response when version is not provided", async () => {
        await supertest(getServer({ logger })).get("/api/update")
            .expect(400)
            .then(res => {
                expect(res.body.status).toEqual(400);
                expect(res.body.code).toEqual(errors.VERSION_NOT_PROVIDED);
            });
    });
    it("should send correct response when version is same", async () => {
        const mockFn = jest.fn(['http://testdownloadurl']);
        const mockDB = { ref: () => ({ get: () => ({ val: () => '2.0.4' }) }) };
        const mockBucket = { file: () => ({ getSignedUrl: mockFn }) };

        await supertest(getServer({ db: mockDB, logger })).get("/api/update?version=2.0.4")
            .expect(200)
            .then(res => {
                expect(res.body).toEqual([]);
                expect(mockBucket.file().getSignedUrl).not.toBeCalled();
            });
    });
    it("should send correct response when version is not same", async () => {
        const mockDB = { ref: () => ({ get: () => ({ val: jest.fn().mockReturnValue('testchecksum') }) }) };
        const mockFn = jest.fn(() => ['http://testdownloadurl']);
        const mockBucket = { file: () => ({ getSignedUrl: mockFn }) };

        await supertest(getServer({ db: mockDB, bucket: mockBucket, logger })).get("/api/update?version=2.0.4")
            .expect(200)
            .then(res => {
                expect(res.body).toEqual(['http://testdownloadurl', 'testchecksum']);
                expect(mockFn).toBeCalled();
            });
    });
});
