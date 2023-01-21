import { join, dirname, resolve } from 'path';
import { readFileSync } from 'fs';
import { parse } from 'yaml';
import { fileURLToPath } from 'url';
import { createWriteStream, mkdirSync, existsSync } from 'fs';
import StreamZip from 'node-stream-zip';
import axios from 'axios';

const __dirname = dirname(fileURLToPath(import.meta.url));

const docs = createWriteStream(join(__dirname, 'docs.zip'));

const docFile = readFileSync(join(__dirname, 'swagger.yml'), 'utf8');

const data = {
    spec: parse(docFile),
    type: 'CLIENT',
    lang: 'dynamic-html'
}

try {
    const res = await axios.post('https://generator3.swagger.io/api/generate', data, { responseType: 'stream' });
    res.data.pipe(docs);

    docs.on('error', err => {
        // console.log(err.stack);
        docs.close();
    });
    docs.on("finish", async () => {
        docs.close();

        const zip = new StreamZip.async({ file: join(__dirname, 'docs.zip') });
        if (!existsSync(resolve('../public/docs'))) mkdirSync(resolve('../public/docs'));
        await zip.extract('docs', '../public/docs');
        await zip.close();
    });
} catch (error) {
    console.log(error);
}