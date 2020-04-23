
package com.onecloud.tcmgmt.web;

import com.onecloud.tcmgmt.dao.TestCaseDao;
import com.onecloud.tcmgmt.dao.UserDao;
import com.onecloud.tcmgmt.domain.appdb.TestCase;
import com.onecloud.tcmgmt.domain.appdb.TestStep;
import com.onecloud.tcmgmt.domain.appdb.User;
import com.onecloud.tcmgmt.semantic.dto.TestCaseDTO;
import com.onecloud.tcmgmt.service.UserStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Controller
@SessionAttributes("testCaseDTO")
public class TestCaseFormController {

    private static final Logger logger = LoggerFactory
            .getLogger(TestCaseFormController.class);

    private final TestCaseDao dao;

    @Autowired
    public TestCaseFormController(TestCaseDao dao) {
        this.dao = dao;
    }

    @RequestMapping(value = "/testcase_form.html",method = RequestMethod.GET)
    public ModelAndView setupForm(@RequestParam(value = "id", required = false) Long id,
                            @RequestParam(value = "addstep", required = false) boolean addStep,
                                  HttpServletRequest request) {
        TestCaseDTO testCaseDTO;
        if (id == null ) {
            logger.debug("ID is null so must be coming from the Add new Test or step");
            if(!addStep) {
                logger.debug("add step is false so must be coming from the Add new Test");
                testCaseDTO = new TestCaseDTO();
            }else{

                testCaseDTO = (TestCaseDTO) request.getSession().getAttribute("testCaseDTO");
                logger.debug("add step is true so must be coming from the Add new step: "+ testCaseDTO.getId());
            }
        }else{
            logger.debug("ID is not null so must be coming from Edit: "+id);
            testCaseDTO = this.dao.getById(id).convertToDTO();
        }
        return new ModelAndView("testcaseForm").addObject(testCaseDTO);
    }

    @RequestMapping(value = "/teststep_add.html", method = RequestMethod.GET)
    public String addStep(@RequestParam(value = "id", required = false) Long id, HttpServletRequest request) {
        logger.debug("INSIDE THE STEP ADD CONTROLLER");
        TestCaseDTO testCaseDTO = (TestCaseDTO) request.getSession().getAttribute("testCaseDTO");
        TestStep testStep = new TestStep();
        if (id == null) {
            logger.debug("INSIDE THE GET: new test case creation");
        } else {
            logger.debug("INSIDE THE GET: Update an test case flow:"+testCaseDTO.getId());
            testStep.setTestCaseId(testCaseDTO.getId());
        }
        if(testCaseDTO.getTestSteps()==null || testCaseDTO.getTestSteps().size() == 0){
            testStep.setTestStepOrder(new Long(1));
        }else{
            testStep.setTestStepOrder(new Long(testCaseDTO.getTestSteps().size()+1));
        }

        testCaseDTO.getTestSteps().add(testStep);

        return "redirect:testcase_form.html?addstep=true";
    }

    @RequestMapping(value = "/testcase_form.html", method = RequestMethod.POST)
    public String processSubmit(@ModelAttribute("testCaseDTO") @Valid TestCaseDTO validatedTestCaseDTO,
                                BindingResult result, SessionStatus status, HttpServletRequest request) {
        if (!result.hasErrors()) {
            try {
                logger.debug("INSIDE THE FORM POST");
                logger.debug("The id in testCaseDTO: " + validatedTestCaseDTO.getId());

                // remove empty steps.
                List<TestStep> dtoSteps = validatedTestCaseDTO.getTestSteps();
                for (int i=0; i<validatedTestCaseDTO.getTestSteps().size(); i++){
                    logger.debug("The test case step id:"+ dtoSteps.get(i).getId() +" test case id:"+dtoSteps.get(i).getTestCaseId());
                    if((dtoSteps.get(i).getTestStep()==null || dtoSteps.get(i).getTestStep().isEmpty() )||
                            (dtoSteps.get(i).getTestStepResult()==null || dtoSteps.get(i).getTestStepResult().isEmpty() )) {
                        logger.debug("INSIDE THE removal step");
                        dtoSteps.remove(i);
                    }
                }

                TestCaseDTO testCaseDTO = (TestCaseDTO) request.getSession().getAttribute("testCaseDTO");
                TestCase testCase;
                if (testCaseDTO== null || testCaseDTO.getId()==null){
                     // brand new test case
                     logger.debug("The test case DTO is null or id is null");
                     testCase = new TestCase();
                     testCase.createUpdateFromDTO(validatedTestCaseDTO);
                }else{
                      // update
                    logger.debug("The test case DTO is NOT null and  id is NOT null");
                     testCase = this.dao.getById(testCaseDTO.getId());
                     logger.debug("GOT THE testcase id:"+testCase.getId());
                    logger.debug("GOT THE testcase id:"+validatedTestCaseDTO.getTestType());
                    logger.debug("GOT THE testcase id:"+validatedTestCaseDTO.getTestSteps().size());
                     testCase.createUpdateFromDTO(validatedTestCaseDTO);
                }
                logger.debug("ABOUT TO create or update");
                this.dao.save(testCase);
                status.setComplete();
                return "redirect:testcase.html?id=" + testCase.getId() + "&success=true";
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                logger.debug("The stack"+ e.getMessage());
                result.reject("DataIntegrityViolationException");
            } catch (ConcurrencyFailureException e) {
                result.reject("ConcurrentModificatonFailure");
            }
        }
        return "testcaseForm";
    }
}
