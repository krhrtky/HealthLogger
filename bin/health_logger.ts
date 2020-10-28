#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from '@aws-cdk/core';
import { HealthLoggerStack } from '../lib/health_logger-stack';

const app = new cdk.App();
new HealthLoggerStack(app, 'HealthLoggerStack');
