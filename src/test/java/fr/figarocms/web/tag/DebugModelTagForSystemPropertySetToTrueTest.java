package fr.figarocms.web.tag;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DebugModelTagForSystemPropertySetToTrueTest extends AbstractDebugModelTagTest {


    @Before
    public void setUp() throws Exception {
        super.setUp();
        overrideConstantField(field, true);
    }


    @Test
    public void test_system_get_property_set_to_true_and_outputDebugModelInJSON_called_three_times() throws Exception {


        mockDebugModelTag.doStartTag();
        mockDebugModelTag.doStartTag();
        mockDebugModelTag.doStartTag();

        verify(mockDebugModelTag, times(3)).outputDebugModelInJSON();


    }


}
