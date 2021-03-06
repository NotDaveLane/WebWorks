/*
* Copyright 2010-2011 Research In Motion Limited.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package blackberry.invoke.messageArguments;

import net.rim.blackberry.api.mail.Message;
import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;

/**
 * This class represents the MessageArgumentsObject
 * 
 * @author sgolod
 * 
 */
public class MessageArgumentsObject extends ScriptableObjectBase {
    private final Message _message;

    public static final String FIELD_VIEW = "view";

    /**
     * Constructs a new MessageArgumentsObject object.
     * 
     * @param m
     *            the Message represented by the class.
     */
    public MessageArgumentsObject( final Message m ) {
        _message = m;
        initial();
    }

    // Injects fields and methods
    private void initial() {
        addItem( new ScriptField( FIELD_VIEW, new Integer( MessageArgumentsConstructor.VIEW_NEW ), ScriptField.TYPE_INT, false,
                false ) );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( final ScriptField field, final Object newValue ) throws Exception {
        return true;
    }

    /**
     * Internal helper method to get direct access to the MessageArgumentsObject's underlying content.
     * 
     * @return the Message item when opening Messages application.
     */
    public Message getMessage() {
        return _message;
    }

    /**
     * Internal helper method to get direct access to the MessageArgumentsObject's underlying content.
     * 
     * @return the type of view when opening Messages application.
     */
    public int getView() {
        final Integer i = (Integer) getItem( FIELD_VIEW ).getValue();
        final int view = i.intValue();
        return view;
    }
}
