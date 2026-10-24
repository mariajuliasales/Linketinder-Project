import 'bootstrap/dist/css/bootstrap.min.css';
import './style.css';
import { resetStorageIfOutdated } from './data/storage';
import { startRouter } from './router';

resetStorageIfOutdated();
startRouter();
