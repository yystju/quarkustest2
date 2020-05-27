package shi.quan;

import io.quarkus.test.junit.QuarkusTest;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.psplib.PSPLIBBaseListener;
import shi.quan.psplib.PSPLIBLexer;
import shi.quan.psplib.PSPLIBParser;

import java.io.IOException;
import java.io.InputStream;

@QuarkusTest
public class PSPLIBParserTest {
    private static final Logger logger = LoggerFactory.getLogger(PSPLIBParserTest.class);

    @Test
    public void test() throws IOException {
        InputStream ins = PSPLIBParserTest.class.getResourceAsStream("/j120.sm/j1201_1.sm");
//        InputStream ins = PSPLIBParserTest.class.getResourceAsStream("/example.sm");

        CharStream stream = CharStreams.fromStream(ins);

        PSPLIBLexer lexer = new PSPLIBLexer(stream);

        PSPLIBParser parser = new PSPLIBParser(new CommonTokenStream(lexer));

        parser.addParseListener(new PSPLIBBaseListener() {
            @Override
            public void exitKey(PSPLIBParser.KeyContext ctx) {
                logger.info("[exitKey] {}", ctx.getText());
                super.exitKey(ctx);
            }

            @Override
            public void exitValue(PSPLIBParser.ValueContext ctx) {
                logger.info("[exitValue] {}", ctx.getText());
                super.exitValue(ctx);
            }

            @Override
            public void enterResource(PSPLIBParser.ResourceContext ctx) {
                logger.info("[enterResource] {}", ctx.getText());
                super.enterResource(ctx);
            }

            @Override
            public void enterProjectInformation(PSPLIBParser.ProjectInformationContext ctx) {
                logger.info("[enterProjectInformation] {}", ctx.getText());
                super.enterProjectInformation(ctx);
            }

            @Override
            public void enterPrecedenceRelations(PSPLIBParser.PrecedenceRelationsContext ctx) {
                logger.info("[enterPrecedenceRelations] {}", ctx.getText());
                super.enterPrecedenceRelations(ctx);
            }



            @Override
            public void exitHeader(PSPLIBParser.HeaderContext ctx) {
                logger.info("[exitHeader] {}", ctx.getText());
                super.exitHeader(ctx);
            }

            @Override
            public void exitRow(PSPLIBParser.RowContext ctx) {
                logger.info("[exitRow] {}", ctx.getText());
                super.exitRow(ctx);
            }

            @Override
            public void exitOthers(PSPLIBParser.OthersContext ctx) {
                logger.info("[exitOthers] {}", ctx.getText());
                super.exitOthers(ctx);
            }

            @Override
            public void visitErrorNode(ErrorNode node) {
                logger.info("[visitErrorNode] {}", node.getText());
                super.visitErrorNode(node);
            }
        });

        PSPLIBParser.ModelContext model = parser.model();

        ins.close();
    }
}