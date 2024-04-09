package cc.ayakurayuki.repo.urls;

import cc.ayakurayuki.repo.urls.Cases.ParseRequestURLTest;
import cc.ayakurayuki.repo.urls.Cases.URLTest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author Ayakura Yuki
 * @date 2024/04/08-15:41
 */
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 5)
@Fork(1)
@Threads(4)
public class CaseBenchmark {

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(CaseBenchmark.class.getSimpleName())
        .result("CaseBenchmark-report.json")
        .resultFormat(ResultFormatType.JSON)
        .build();
    new Runner(opt).run();
  }

  private List<URL> inputs;

  @Setup(Level.Trial)
  public void init() {
    inputs = new ArrayList<>();
    for (URLTest tt : Cases.urlTests) {
      URL u = URLs.Parse(tt.in());
      if (Strings.isEmpty(tt.roundtrip())) {
        continue;
      }
      inputs.add(u);
    }
  }

  @TearDown(Level.Trial)
  public void tearDown() {
    inputs.clear();
    inputs = null;
  }

  @Benchmark
  public void BenchmarkToString(Blackhole blackhole) {
    for (URL u : inputs) {
      blackhole.consume(u.toString());
    }
  }

  @Benchmark
  public void BenchmarkParse(Blackhole blackhole) {
    for (URLTest tt : Cases.urlTests) {
      blackhole.consume(URLs.Parse(tt.in()));
    }
  }

  @Benchmark
  public void BenchmarkParseRequestURI(Blackhole blackhole) {
    for (ParseRequestURLTest tt : Cases.parseRequestURLTests) {
      try {
        blackhole.consume(URLs.ParseRequestURI(tt.url));
      } catch (Exception ignored) {
      }
    }
  }

  @Benchmark
  public void BenchmarkResolvePath(Blackhole blackhole) {
    blackhole.consume(URLs.resolvePath("a/b/c", ".././d"));
  }

}
