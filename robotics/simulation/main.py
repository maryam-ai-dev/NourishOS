import structlog

log = structlog.get_logger()


class SimulationRunner:
    """Stub for the NourishOS smart kitchen execution simulation."""

    def __init__(self):
        self.running = False

    def start(self):
        self.running = True
        log.info("simulation_runner_ready")

    def stop(self):
        self.running = False
        log.info("simulation_runner_stopped")


if __name__ == "__main__":
    runner = SimulationRunner()
    runner.start()
