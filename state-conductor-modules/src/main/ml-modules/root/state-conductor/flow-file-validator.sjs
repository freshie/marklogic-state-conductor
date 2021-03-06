'use strict';

// TODO utilize existing json schemas https://github.com/airware/asl-validator/

const schema = {
  properties: {
    Comment: { type: 'string' },
    StartAt: { type: 'string' },
    States: {
      type: 'object',
      patternProperties: {
        '^.{1,128}$': { 
          type: 'object',
          oneOf: [
            {
              type: 'object',
              properties: {
                Type: {
                  type: 'string',
                  pattern: '^Pass$'
                }
              }
            },
            {
              type: 'object',
              properties: {
                Type: {
                  type: 'string',
                  pattern: '^Succeed$'
                }
              }
            },
            {
              type: 'object',
              properties: {
                Type: {
                  type: 'string',
                  pattern: '^Fail$'
                }
              }
            },
            {
              type: 'object',
              properties: {
                Type: {
                  type: 'string',
                  pattern: '^Task$'
                }
              }
            },
            {
              type: 'object',
              properties: {
                Type: {
                  type: 'string',
                  pattern: '^Choice$'
                }
              }
            }
          ]
        }
      },
      additionalProperties: false
    },
    Version: { type: 'string' },
    mlDomain: {
      type: 'object',
      properties: {
        context: {
          type: 'array',
          minItems: 1,
          items: {
            type: 'object',
            properties: {
              scope: { type: 'string' },
              value: { type: 'string' }
            },
            additionalProperties: false,
            required: ['scope', 'value']          
          }
        }
      },
      additionalProperties: false,
      required: ['context']
    }
  },
  additionalProperties: false,
  required: ['StartAt', 'States', 'mlDomain']
};


/**
 * Given a state conductor flow, validate against the
 * MarkLogic implementation of Amazon State Language
 *
 * @param {*} flow
 * @returns
 */
function validateFlowFile(flow) {
  try {
    xdmp.jsonValidateNode(flow, schema);
    return true;
  } catch(err) {
    return false;
  }
}

module.exports = {
  validateFlowFile
};