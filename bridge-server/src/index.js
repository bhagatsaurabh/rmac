import * as dotenv from 'dotenv';
dotenv.config({ path: `../.env.${process.env.NODE_ENV}` });

import server from './servers/socket.js';

const listener = server.listen(process.env.PORT || 80, () => {
    console.log(`Bridge Server started on ${listener.address().port}`);
});
