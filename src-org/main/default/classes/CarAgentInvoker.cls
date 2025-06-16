public with sharing class CarAgentInvoker {
    public static void bookTestDrive() {
        try {
            // Create an instance of the invocable action
            Invocable.Action action = Invocable.Action.createCustomAction(
                'Book_Test_Drive','btd'
            );

            // Set invocation parameters
            action.setInvocationParameter('userMessage', 'Book a Test Drive for Zig M3');

            // Execute the action
            List<Invocable.Action.Result> results = action.invoke();
            Invocable.Action.Result result = results[0];

            // Handle response
            if (result.isSuccess()) {
                // Retrieve the Session id and Agent Response
               System.debug('Output Session Id: ' + result.getOutputParameters().get('sessionId'));
               System.debug('Output Agent Response: ' + result.getOutputParameters().get('agentResponse'));
            } else {
                System.debug('Agent action execution failed: ' + result.getErrors());
            }
        } catch (Exception e) {
            System.debug('Error invoking Agent action: ' + e.getMessage());
        }
    }
}