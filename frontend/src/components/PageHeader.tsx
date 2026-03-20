import { Panel } from "./ui/Panel";

export function PageHeader() {
  return (
    <Panel as="header" className="bg-white/80 p-6 backdrop-blur">
      <h1 className="mt-2 text-2xl font-black sm:text-3xl">핫딜 모아보기</h1>
    </Panel>
  );
}
