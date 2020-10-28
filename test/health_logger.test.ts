import { expect as expectCDK, matchTemplate, MatchStyle } from '@aws-cdk/assert';
import * as cdk from '@aws-cdk/core';
import * as HealthLogger from '../lib/health_logger-stack';

test('Empty Stack', () => {
    const app = new cdk.App();
    // WHEN
    const stack = new HealthLogger.HealthLoggerStack(app, 'MyTestStack');
    // THEN
    expectCDK(stack).to(matchTemplate({
      "Resources": {}
    }, MatchStyle.EXACT))
});
