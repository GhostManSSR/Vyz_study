import Head from "next/head";
import Image from "next/image";
import Button from "@/component/layout/Button";
import Toggle from "@/component/layout/Toggle";
import styles from "@/styles/Home.module.css";
import DefaultForm from "@/component/form/DefaultForm";
import {useRouter} from "next/router";

export default function Home() {
  const router = useRouter();

  return (
    <>
      <Toggle
          toggleList={[{name:'Form', onClick: () => router.push("/form")}]}
      />
    </>
  );
}
